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

private[purerand] trait RNG {
  def nextInt: (RNG, Int) = {
    val (rng, l) = nextLong
    (rng, l.toInt)
  }

  def nextInt(maxValue: Int): (RNG, Int) = {
    val (rng, i) = nextInt
    val abs = if (i < 0) -(i + 1) else i
    (rng, abs % maxValue)
  }

  def nextLong: (RNG, Long)

}

private[purerand] object RNG {
  def apply(seed: Seed): RNG = SimpleRNG(seed)

  private case class SimpleRNG(seed: Seed) extends RNG {
    def nextLong: (RNG, Long) = {
      val newSeed = (seed.value * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
      val nextRNG = SimpleRNG(Seed(newSeed))
      val n = (newSeed >>> 16)
      (nextRNG, n)
    }
  }
}