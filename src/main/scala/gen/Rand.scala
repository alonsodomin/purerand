package gen

import cats._
import cats.data._
import cats.implicits._

final case class Rand[A](state: State[RNG, A]) extends AnyVal
object Rand extends RandInstances {
  def const[A](a: A): Rand[A] = Rand(State.pure(a))

  def oneOf[A](seq: NonEmptyVector[A]): Rand[A] = Rand(State { rng =>
    val vec = seq.toVector
    val (r, idx) = rng.nextInt(vec.size)
    (r, vec(idx))
  })

  def option[A](rand: Rand[A]): Rand[Option[A]] = for {
    empty <- boolean
    result <- if (empty) const(none[A]) else rand.map(_.some)
  } yield result

  def listOfN[A](n: Int, rand: Rand[A]): Rand[List[A]] =
    List.fill(n)(rand).sequence

  def int: Rand[Int] = Rand(State(_.nextInt))
  
  def boolean: Rand[Boolean] = Rand(State { rng =>
    val (r, i) = rng.nextInt(2)
    (r, if (i == 0) false else true)
  })

  def double: Rand[Double] = Rand(State { rng =>
    val (r, i) = rng.nextInt(Int.MaxValue)
    (r, i / Int.MaxValue.toDouble + 1)
  })

}

trait RandInstances {
  implicit val randFunctor: Functor[Rand] = new RandFunctor {}

  implicit val randMonad: Monad[Rand] = new RandMonad {}
}

trait RandFunctor extends Functor[Rand] {
  def map[A, B](fa: Rand[A])(f: A => B): Rand[B] =
      Rand(fa.state.map(f))
}

trait RandApplicative extends RandFunctor with Applicative[Rand] {
  def pure[A](x: A): Rand[A] = Rand.const(x)

  def ap[A, B](ff: Rand[A => B])(fa: Rand[A]): Rand[B] =
    Rand(ff.state.ap(fa.state))
}

trait RandMonad extends RandApplicative with Monad[Rand] {
  def flatMap[A, B](fa: Rand[A])(f: A => Rand[B]): Rand[B] =
    Rand(fa.state.flatMap(a => f(a).state))

  def tailRecM[A, B](a: A)(f: A => Rand[Either[A,B]]): Rand[B] =
    Rand(Monad[State[RNG, ?]].tailRecM(a)(a => f(a).state))
}