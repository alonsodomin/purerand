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
package cli

import cats.implicits._

import com.monovore.decline._

final case class Settings(seed: Option[Seed], maxItems: Long, target: Target)
object Settings {
  lazy val opts: Opts[Settings] = {
    val seed = {
      val givenSeed = Opts.option[Long]("seed", "Seed to be used for generating random data", "s").map(x => Some(Seed.fromLong(x)))
      val noSeed = Opts.flag("no-seed", "Use the system clock to get a new seed").map(_ => none[Seed])
      noSeed orElse givenSeed
    }

    val maxItems = Opts.option[Long]("max-items", "Maximum number of items to generate")

    (seed, maxItems).mapN(Settings.apply(_, _, Target.StdOut))
  }
}

sealed trait Target extends Product with Serializable
object Target {
  case object StdOut extends Target
}