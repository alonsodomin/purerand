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

import cats.Show
import cats.effect._
import cats.implicits._

import com.monovore.decline._

import purerand.render._

import scala.concurrent.ExecutionContext

object PurerandCLI {
  private implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def main[A: Show](rand: Rand[A])(args: List[String]): IO[ExitCode] = {
    mainCmd(rand).parse(args) match {
      case Right(action) => action
      case Left(help) =>
        IO(println(help.toString())) >> IO(ExitCode.Error)
    }
  }

  private def mainCmd[A: Show](rand: Rand[A]): Command[IO[ExitCode]] = {
    Command("", "")(Settings.opts).map { settings =>
      for {
        seed <- settings.seed.fold(Seed.fromWallClock[IO])(IO.pure)
        renderer <- IO.pure(Renderer.stdout[A])
        _ <- rand.sample(seed).take(settings.maxItems).covary[IO].through(renderer).compile.drain
      } yield ExitCode.Success
    }
  }
}