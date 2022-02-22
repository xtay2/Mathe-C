package simpson;

import static ch.obermuhlner.math.big.BigDecimalMath.exp;
import static ch.obermuhlner.math.big.BigDecimalMath.pi;
import static ch.obermuhlner.math.big.BigDecimalMath.pow;
import static ch.obermuhlner.math.big.BigDecimalMath.sqrt;
import static java.lang.System.out;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

abstract class Simpson {

	// Constants for Big Decimals
	static final BigDecimal TWO = valueOf(2);
	static final BigDecimal FOUR = valueOf(4);
	static final BigDecimal SIX = valueOf(6);
	static final BigDecimal NINE = valueOf(9);

	// Constants for A3
	static final BigDecimal cn = new BigDecimal("0.388034908872");
	static final BigDecimal alphaBound = new BigDecimal("0.05");

	/** Precison -> Amount of Decimal Digits. */
	static final MathContext P = new MathContext(8);

	public static void main(String[] args) {
		A1c();
		A2_();
		A3b();
		A3d();
		A3e();
		A4b();
	}

	/** Test for f(x) = x^3 - (2 * x^2) + 1 */
	static void A1c() {
		Function f = x -> x.pow(3).subtract(TWO.multiply(x.pow(2))).add(ONE);
		out.println("\n1c: " + integral(f, ZERO, ONE, 1));
	}

	/** Approximate upper border. */
	static void A2_() {
		// 2) g(x) = -x^2 / 2
		Function g = x -> exp(x.pow(2).negate().divide(TWO), P);

		// 1 / sqrt(2 * pi)
		BigDecimal k = ONE.divide(sqrt(TWO.multiply(pi(P)), P), P);

		final BigDecimal INF = approxUpperBorder(g, P); // Positive Infinity for approximation

		out.println("\n2: " + k.multiply(integral(g, ZERO, INF, P), P));
	}

	/** Approximation for constants. */
	static void A3b() {
		// 3 <= c <= 9
		for (int c = 3; c <= 9; c++) {

			final BigDecimal n = valueOf(c);

			Function h = x -> pow(ONE.add(x.pow(2).divide(n, P)), n.add(ONE).divide(TWO).negate(), P);
			out.println("\n3b for n = " + n + ": " + ONE.divide(integral(h, valueOf(0), approxUpperBorder(h, P), P).multiply(TWO), P));

		}
	}

	/** Calculate lower bound for alpha = 0.05. */
	static void A3d() {

		final Function f = x -> pow(ONE.add(x.pow(2).divide(NINE, P)), NINE.add(ONE).divide(TWO).negate(), P);

		final BigDecimal upper = approxUpperBorder(f, P);

		// Current alpha, initialised to be bigger than alphaBound
		BigDecimal alpha = alphaBound.add(ONE);

		// Lowest xi, initialised to be unprecise, but produce an alpha > alphabound
		BigDecimal lowerBound = TWO;

		// The increasing precision
		BigDecimal precision = new BigDecimal("0.1");

		// For P decimal-digits of precision...
		for (int i = 0; i < P.getPrecision(); i++) {
			// ...approximate the lower bound for lower < alpha bound with the increasing precision i
			for (BigDecimal lower = lowerBound; true; lower = lower.add(precision)) {
				alpha = integral(f, lower, upper, 100).multiply(TWO).multiply(cn);
				if (alpha.compareTo(alphaBound) <= 0)
					break;
				lowerBound = lower;
			}
			out.println("3d) Alpha = " + alpha + " for xi = " + lowerBound + " with precision " + i);
			precision = precision.divide(TEN);
		}
	}

	/** Difference to alpha. */
	static void A3e() {
		final Function f = x -> pow(ONE.add(x.pow(2).divide(NINE, P)), NINE.add(ONE).divide(TWO).negate(), P);
		final BigDecimal lower = new BigDecimal("2.26216388");
		final BigDecimal upper = approxUpperBorder(f, P);

		out.println("\n3e) " + integral(f, lower, upper, P).multiply(TWO).multiply(cn).subtract(alphaBound, P).abs().toPlainString());
	}

	/** Transformed variable T. */
	static void A4b() {
		final List<BigDecimal> data = List.of("1.6", "1.8", "1.9", "1.4", "3.3", "1.6", "1.7", "1.3", "3.4") // Values
				.stream().map(e -> new BigDecimal(e)).collect(Collectors.toList()); // To BigDecimal

		// Average Value of data
		final BigDecimal avg = data.stream().map(Objects::requireNonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(valueOf(data.size()));

		final BigDecimal n = NINE;

		// Expected Value = 1.5
		final BigDecimal expect = new BigDecimal("1.5");

		BigDecimal temp = ZERO;
		for (BigDecimal X_i : data)
			temp = temp.add(X_i.subtract(avg).pow(2));

		// Standard-Deviation Squared
		BigDecimal Ssquare = temp.multiply(ONE.divide(n.subtract(ONE)));

		out.println("\n4b) S^2 = " + Ssquare);

		out.println("\n4b) T = " + n.sqrt(P).divide(Ssquare.sqrt(P), P).multiply(avg.subtract(expect)));
	}

	/**
	 * Approximates the upper border for an interval of an integral within a given precision.
	 * 
	 * @param f         is the function.
	 * @param precision is the wanted decimal digit precision.
	 * @return
	 */
	static BigDecimal approxUpperBorder(Function f, MathContext precision) {
		return recUpperBorder(f, ZERO, ONE, ONE.scaleByPowerOfTen(-precision.getPrecision()));
	}

	/**
	 * Approximates the upper border for an interval of an integral within a given precision recursivly.
	 * 
	 * @param f     is the function.
	 * @param x     is the lower bound (starting value).
	 * @param range is the step (increases exponentially: range *= 2)
	 * @param p     is is the precision.
	 * @return the nearest precise upper border but up too have the double precision of whats necessary.
	 */
	static BigDecimal recUpperBorder(Function f, BigDecimal x, BigDecimal range, BigDecimal p) {
		if (diff(f.at(x), f.at(x.add(range))).compareTo(p) > 0)
			return recUpperBorder(f, x.add(range), range.multiply(TWO), p);
		return x;
	}

	/** Returns the absolute difference of two {@link BigDecimal}s. */
	static BigDecimal diff(BigDecimal a, BigDecimal b) {
		return a.subtract(b).abs();
	}

	/**
	 * Approximates the Integral of the given function f in the given interval with the simpson-formula
	 * and an estimated precision.
	 * 
	 * @param f     is the {@link Function}.
	 * @param start is the start x of the Interval.
	 * @param end   is the end x of the Interval.
	 * @param m     is the {@link MathContext} that dictates the precision.
	 * @return the area in decimal units.
	 */
	static BigDecimal integral(Function f, final BigDecimal start, final BigDecimal end, MathContext m) {
		BigDecimal a = integral(f, start, end, 1);
		for (int i = 1;; i *= 2) {
			BigDecimal b = integral(f, start, end, i * 2);
			if (diff(a, b).compareTo(ONE.scaleByPowerOfTen(-m.getPrecision())) < 0) {
				out.println("Estimated parts: " + i);
				return b;
			}
			a = b;
		}
	}

	/**
	 * Approximates the Integral of the given function f in the given interval with the simpson-formula.
	 * 
	 * @param f     is the {@link Function}.
	 * @param start is the start x of the Interval.
	 * @param end   is the end x of the Interval.
	 * @param parts is the number of sub-intervals.
	 * @return the area in decimal units.
	 */
	static BigDecimal integral(Function f, final BigDecimal start, final BigDecimal end, final int parts) {
		if (start.compareTo(end) > 0)
			throw new IllegalArgumentException("Start cannot be bigger then end.");
		if (parts <= 0)
			throw new IllegalArgumentException("Increment has to be positive.");

		final BigDecimal N = valueOf(parts);
		final BigDecimal h = end.subtract(start).divide(N, P);

		// x(i) = a + i * h
		final Function x = i -> start.add(i.multiply(h));

		// f(x(0))
		BigDecimal area = f.at(x.at(ZERO));

		BigDecimal temp = BigDecimal.ZERO;

		// 1 -> N-1: f(x(i)) + f(x(N))
		for (BigDecimal i = ONE; i.compareTo(N.subtract(ONE)) <= 0; i = i.add(ONE))
			temp = temp.add(f.at(x.at(i)));

		// f(x(0)) + 2 * fst_sum
		area = area.add(TWO.multiply(temp)).add(f.at(x.at(N)));

		temp = BigDecimal.ZERO;
		// 1 -> N: f((x(i-1) + x(i)) / 2)
		for (BigDecimal i = ONE; i.compareTo(N) <= 0; i = i.add(ONE))
			temp = temp.add(f.at((x.at(i.subtract(ONE)).add(x.at(i))).divide(TWO)));

		// f(x(0)) + 2 * fst_sum + 4 * snd_sum
		area = area.add(FOUR.multiply(temp));

		// h/6 * (f(x(0)) + 2 * fst_sum + 4 * snd_sum)
		return h.divide(SIX, P).multiply(area, P);
	}

	/** A 2D Function f(x) */
	@FunctionalInterface
	interface Function {
		BigDecimal at(BigDecimal x);
	}
}