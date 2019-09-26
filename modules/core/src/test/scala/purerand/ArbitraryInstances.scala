/*
 * Copyright (c) 2019 A. Alonso Dominguez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package purerand

import org.scalacheck.{Arbitrary, Cogen}

private[purerand] trait ArbitraryInstances {
  import Arbitrary.arbitrary

  implicit val arbitrarySeed: Arbitrary[Seed] =
    Arbitrary(arbitrary[Long].map(Seed.fromLong))

  implicit def arbitraryRand[A](implicit arbA: Arbitrary[A]): Arbitrary[Rand[A]] =
    Arbitrary(arbA.arbitrary.map(Rand.const))

  implicit def arbitraryRandFn[A, B](implicit arbFn: Arbitrary[A => B]): Arbitrary[Rand[A => B]] =
    Arbitrary(arbFn.arbitrary.map(Rand.const))

  implicit def cogenRand[A: Cogen]: Cogen[Rand[A]] =
    Cogen((seed, rand) => Cogen[A].perturb(seed, rand.single(Seed.fromLong(seed.long._1))))

}