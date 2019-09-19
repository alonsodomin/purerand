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

import cats.{Eq, Functor}
import cats.effect.Clock
import cats.implicits._

import java.util.concurrent.TimeUnit

final case class Seed private (value: Long) extends AnyVal {

  def nextLong: (Seed, Long) = {
    val newSeed  = (value * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextSeed = Seed(newSeed)
    val n        = (newSeed >>> 16)
    (nextSeed, n)
  }

  def nextInt: (Seed, Int) = {
    val (seed, l) = nextLong
    (seed, l.toInt)
  }

  def nextInt(maxValue: Int): (Seed, Int) = {
    val (seed, i) = nextInt
    val abs       = if (i < 0) -(i + 1) else i
    (seed, abs % maxValue)
  }

  def nextDouble: (Seed, Double) = {
    val (seed, i) = nextInt(Int.MaxValue)
    (seed, i / Int.MaxValue.toDouble + 1)
  }

}
object Seed {
  def fromLong(value: Long): Seed = Seed(value)

  def fromWallClock[F[_]: Functor](implicit clock: Clock[F]): F[Seed] =
    clock.monotonic(TimeUnit.MICROSECONDS).map(fromLong)

  implicit val seedEq: Eq[Seed] = Eq.by(_.value)
}
