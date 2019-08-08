package binaryGap

object Solution {


  def main(args: Array[String]): Unit = {
    //climbingLeaderboard(Array(100, 90, 90, 80, 75, 60), Array(50, 65, 77, 90, 102))

    val matrix = matrixSolving("XMJYAUZ", "MZJAWXU")
    val ret = solution("XMJYAUZ", "MZJAWXU", 7, 7, matrix)
    println(ret)
  }


  def matrixSolving(input1: String, input2: String): Array[Array[Int]] = {

    val lenInput1 = input1.length
    val lenInput2 = input2.length
    val matrix = Array.ofDim[Int](lenInput1 + 1, lenInput2 + 1)

    for (i <- 1 to lenInput2) {
      var found = false
      for (j <- 1 to lenInput1) {
        if (input1(i - 1) == input2(j - 1)) {
          matrix(i)(j) = matrix(i - 1)(j) + 1
          found = true
        }
        else if (found) {
          matrix(i)(j) = math.max(matrix(i)(j - 1), matrix(i - 1)(j))
        }
        else {
          matrix(i)(j) = matrix(i - 1)(j)
        }

      }


    }


    matrix
  }

  def solution(input1: String, input2: String, lenInput1: Int, lenInput2: Int, matrix: Array[Array[Int]]): String = {

    println(s"\t lenInput1 $lenInput1 lenInput2 $lenInput2 ")
    if (lenInput1 == 0 || lenInput2 == 0) {
      return ""
    }

    if (input1(lenInput1 - 1) == input2(lenInput2 - 1)) {
      return solution(input1, input2, lenInput1 - 1, lenInput2 - 1, matrix) + input1(lenInput1 - 1)
    }

    val bool = matrix(lenInput1 - 1)(lenInput2) > matrix(lenInput1)(lenInput2 - 1)
    val rpos = lenInput1 - 1
    val cpos = lenInput2 - 1
    val value1 = matrix(lenInput1 - 1)(lenInput2)
    val value2 = matrix(lenInput1)(lenInput2 - 1)
    println(s" position ($rpos,$lenInput2)  ($lenInput1,$cpos)   bool=$bool value = $value1 , $value2")
    if (matrix(lenInput1 - 1)(lenInput2) > matrix(lenInput1)(lenInput2 - 1)) {
      return solution(input1, input2, lenInput1 - 1, lenInput2, matrix)
    } else {
      return solution(input1, input2, lenInput1, lenInput2 - 1, matrix)
    }

  }


}
