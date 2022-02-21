package simpson;

import java.math.BigDecimal;
import java.math.MathContext;

import static ch.obermuhlner.math.big.BigDecimalMath.*;

import static java.math.BigDecimal.*;

abstract class Simpson {
 
 	// Constants for Big Decimals
 	static final BigDecimal TWO = valueOf(2);
 	static final BigDecimal FOUR = valueOf(4);
 	static final BigDecimal SIX = valueOf(6);
 
 	/** Precison -> Amount of Decimal Digits. */
 	static final MathContext P = new MathContext(8);
 
 	public static void main(String[] args) {
 		
 		// 1b) f(x) = x^3 - (2 * x^2) + 1
 		Function f = x -> x.pow(3).subtract(TWO.multiply(x.pow(2))).add(ONE);
 
 		System.out.println("A1: " + integral(f, ZERO, ONE, 1));
 
 		// 1c) g(x) = -x^2 / 2
 		Function g = x -> exp(x.pow(2).negate().divide(TWO), P);
 
 		// 1 / sqrt(2 * pi)
 		BigDecimal k = ONE.divide(sqrt(TWO.multiply(pi(P)), P), P);
 
 		final BigDecimal INF = approxUpperBorder(g, P); // Positive Infinity for approximation
 
 		System.out.println("A2: " + k.multiply(integral(g, ZERO, INF, P), P));
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
 		BigDecimal d = (a.compareTo(b) < 0 ? b : a).subtract(a.compareTo(b) < 0 ? a : b).abs();
 //		System.out.println("Diff: " + d);
 		return d;
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
 				System.out.println("Estimated parts: " + i);
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