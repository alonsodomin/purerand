package gen

trait RNG {
  def nextInt: (RNG, Int)

  def nextInt(maxValue: Int): (RNG, Int) = {
    val (rng, i) = nextInt
    val abs = if (i < 0) -(i + 1) else i
    (rng, abs % maxValue)
  }

}

case class SimpleRNG(seed: Long) extends RNG {
  def nextInt: (RNG, Int) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRNG = SimpleRNG(newSeed)
    val n = (newSeed >>> 16).toInt
    (nextRNG, n)
  }
}