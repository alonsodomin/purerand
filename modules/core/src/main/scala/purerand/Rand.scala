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

  def single(seed: Seed): A =
    state.runA(seed).value

  def sample(seed: Seed): Stream[Pure, A] =
    Stream.unfold(seed)(state.run(_).value.swap.some)

}
object Rand extends RandInstances {

  def apply[A](f: Seed => (Seed, A)): Rand[A] =
    new Rand(State(f))

  def const[A](a: A): Rand[A] = new Rand(State.pure(a))

  def unit: Rand[Unit] = const(())

  def oneOf[A](seq: NonEmptyVector[A]): Rand[A] =
    Rand { seed =>
      val vec            = seq.toVector
      val (newSeed, idx) = seed.nextInt(vec.size)
      (newSeed, vec(idx))
    }

  def option[A](rand: Rand[A]): Rand[Option[A]] =
    for {
      empty  <- boolean
      result <- if (empty) const(none[A]) else rand.map(_.some)
    } yield result

  def listOfN[A](n: Int, rand: Rand[A]): Rand[List[A]] =
    List.fill(n)(rand).sequence

  def int: Rand[Int] = Rand(_.nextInt)

  def boolean: Rand[Boolean] =
    Rand { seed =>
      val (r, i) = seed.nextInt
      (r, if (i % 2 == 0) false else true)
    }

  def double: Rand[Double] =
    Rand(_.nextDouble)

  def weighted[A](rands: NonEmptyList[(Int, Rand[A])]): Rand[A] = {
    val allRands = rands.flatMap {
      case (weight, rand) =>
        NonEmptyList.fromListUnsafe(List.fill(weight)(rand))
    }
    oneOf(NonEmptyVector.fromVectorUnsafe(allRands.toList.toVector)).flatten
  }

}

private[purerand] trait RandInstances {
  implicit val randMonad: Monad[Rand] with FunctorFilter[Rand] = new RandInstance

  implicit def randEq[A: Eq]: Eq[Rand[A]] = Eq.instance { (left, right) =>
    val seed      = Seed.fromLong(1L)
    val nextLeft  = left.single(seed)
    val nextRight = right.single(seed)
    nextLeft === nextRight
  }
}

private[purerand] final class RandInstance extends Monad[Rand] with FunctorFilter[Rand] {
  def functor: Functor[Rand] = this

  def pure[A](x: A): Rand[A] = Rand.const(x)

  def flatMap[A, B](fa: Rand[A])(f: A => Rand[B]): Rand[B] =
    new Rand(fa.state.flatMap(a => f(a).state))

  def tailRecM[A, B](a: A)(f: A => Rand[Either[A, B]]): Rand[B] =
    new Rand(Monad[State[Seed, *]].tailRecM(a)(a => f(a).state))

  def mapFilter[A, B](fa: Rand[A])(f: A => Option[B]): Rand[B] = 
    flatMap(fa) { a =>
      tailRecM(a)(x => pure(f(a).map(Right(_)).getOrElse(Left(x))))
    }

}
