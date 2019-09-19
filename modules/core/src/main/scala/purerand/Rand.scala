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

import cats._
import cats.data._
import cats.implicits._

import fs2.{Pure, Stream}

final class Rand[A] private[purerand] (private[purerand] val state: State[Seed, A]) extends AnyVal {

  def next(seed: Seed): A =
    state.runA(seed).value

  def sample(seed: Seed): Stream[Pure, A] =
    Stream.unfold(seed)(rng => state.run(rng).value.swap.some)

}
object Rand extends RandInstances {

  def const[A](a: A): Rand[A] = new Rand(State.pure(a))

  def unit: Rand[Unit] = const(())

  def oneOf[A](seq: NonEmptyVector[A]): Rand[A] =
    new Rand(State { rng =>
      val vec      = seq.toVector
      val (r, idx) = rng.nextInt(vec.size)
      (r, vec(idx))
    })

  def option[A](rand: Rand[A]): Rand[Option[A]] =
    for {
      empty  <- boolean
      result <- if (empty) const(none[A]) else rand.map(_.some)
    } yield result

  def listOfN[A](n: Int, rand: Rand[A]): Rand[List[A]] =
    List.fill(n)(rand).sequence

  def int: Rand[Int] = new Rand(State(_.nextInt))

  def boolean: Rand[Boolean] =
    new Rand(State { rng =>
      val (r, i) = rng.nextInt
      (r, if (i % 2 == 0) false else true)
    })

  def double: Rand[Double] =
    new Rand(State { rng =>
      val (r, i) = rng.nextInt(Int.MaxValue)
      (r, i / Int.MaxValue.toDouble + 1)
    })

  def weighted[A](rands: NonEmptyList[(Int, Rand[A])]): Rand[A] = {
    val allRands = rands.flatMap {
      case (weight, rand) =>
        NonEmptyList.fromListUnsafe(List.fill(weight)(rand))
    }
    oneOf(NonEmptyVector.fromVectorUnsafe(allRands.toList.toVector)).flatten
  }

}

private[purerand] trait RandInstances {
  implicit val randMonad: Monad[Rand] = new RandMonad {}

  implicit def randEq[A: Eq]: Eq[Rand[A]] = Eq.instance { (left, right) =>
    val seed      = Seed.fromLong(1L)
    val nextLeft  = left.next(seed)
    val nextRight = right.next(seed)
    nextLeft === nextRight
  }
}

private[purerand] trait RandFunctor extends Functor[Rand] {
  def map[A, B](fa: Rand[A])(f: A => B): Rand[B] =
    new Rand(fa.state.map(f))
}

private[purerand] trait RandApplicative extends RandFunctor with Applicative[Rand] {
  def pure[A](x: A): Rand[A] = Rand.const(x)

  def ap[A, B](ff: Rand[A => B])(fa: Rand[A]): Rand[B] =
    new Rand(ff.state.ap(fa.state))
}

private[purerand] trait RandMonad extends RandApplicative with Monad[Rand] {
  def flatMap[A, B](fa: Rand[A])(f: A => Rand[B]): Rand[B] =
    new Rand(fa.state.flatMap(a => f(a).state))

  def tailRecM[A, B](a: A)(f: A => Rand[Either[A, B]]): Rand[B] =
    new Rand(Monad[State[Seed, ?]].tailRecM(a)(a => f(a).state))
}
