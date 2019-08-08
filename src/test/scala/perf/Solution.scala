package perf

// you can write to stdout for debugging purposes, e.g.
// println("this is a debug message")

class Solution(val size: Int) {

}

object Solution {

  def main(args: Array[String]) {
    val me = Solution(10)
    println(me)
  }

  def apply(size: Int) =
    new Solution(10)


}
