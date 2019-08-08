package binaryGap

import scala.collection.mutable.Stack

object Solution1 {


  def main(args: Array[String]): Unit = {
    //checkAreTheSame(stringToStack("yzfzfyvyvyyygzzvtzyg").reverse,stringToStack("vzzgyyyvyvyfzfzy"))
    println(solution("abcdef edcba cdba cba ba hijkfedcba fdaefedcba edddfedcba"))

    //println(solution( "abcc bc ac"))
    println(solution("fvgfgzggyyf zyfttgytgyvyyyzygv ggzgfgvffzvfytgyvz zvygtyfvzf yvzfvvttyyv gyzyyyvygtygttfyzfyy"))

    //yzfzfyvyvyyygzzvtzyg fzvyzvfftgz zfzzgtffvzyvzfgyzt vzzgyyyvyvyfzfzy

  }

  def solution(s: String): String = {

    val splits = s.split("[\\p{Punct}\\s]+")
    val first = finFirstElement(splits)
    if (!first._1 && !checkStackIsPali(first._2)) {
      return "NO"
    }
    var next = findSome(first._2, first._3, 1, splits)
    if (!next._1) {
      return "NO"
    }
    var canStop = !checkStackIsPali(next._2)
    var newPos = 2
    while (next._1 && canStop) {
      next = findSome(next._2, next._3, newPos, splits)
      canStop = !checkStackIsPali(next._2)
      newPos = newPos + 1
    }

    if (!checkStackIsPali(first._2)) {
      return "NO"
    }

    if (next._3.isEmpty) {
      return "NO"
    }

    next._3.reduce((e1, e2) => e1 + " " + e2)
  }

  def finFirstElement(sentences: Array[String]): (Boolean, Stack[Char], List[String]) = {

    for (k <- 0 until sentences.length) {
      val pharse = sentences(k)
      val ret = findElement(pharse, sentences)
      if (ret._1) {
        return ret
      }
    }
    (false, Stack(), List())
  }

  def findElement(phrase: String, sentences: Array[String]): (Boolean, Stack[Char], List[String]) = {

    val len = sentences.length

    for (k <- 0 until len) {
      val temp = sentences(k)
      val ret = checkAreTheSame(stringToStack(phrase.reverse), stringToStack(temp))
      if (ret._1) {
        return (ret._1, ret._2, List(phrase, temp))
      }
    }

    (false, Stack(), List())

  }

  def stringToStack(input: String): Stack[Char] = {
    val stack: Stack[Char] = Stack()
    input.foreach(e => stack.push(e))
    stack
  }

  def checkAreTheSame(first: Stack[Char], last: Stack[Char]): (Boolean, Stack[Char]) = {

    var found = false

    while (!first.isEmpty && !last.isEmpty && first.head == last.head) {
      first.pop()
      last.pop()
      found = true
    }

    if (first.isEmpty) {
      return (true, last)
    }

    if (last.isEmpty) {
      return (true, first)
    }

    (false, first)
  }

  def findSome(some: Stack[Char], list: List[String], pos: Int, sentences: Array[String]): (Boolean, Stack[Char], List[String]) = {
    if (some.isEmpty) {
      return (true, some, list)
    }

    val len = sentences.length

    for (k <- 0 until len) {
      val temp = sentences(k)
      var ret = checkAreTheSame(some, stringToStack(temp))
      if (ret._1 && !list.contains(temp)) {

        return (true, ret._2, list.take(pos) ++ List(temp) ++ list.drop(pos))
      }

      ret = checkAreTheSame(some, stringToStack(temp.reverse))

      if (ret._1 && !list.contains(temp)) {
        return (true, ret._2, list.take(pos - 1) ++ List(temp) ++ list.drop(pos - 1))

      }
    }

    (false, some, list)
  }

  def checkStackIsPali(some: Stack[Char]): Boolean = {
    if (some.isEmpty) {
      return true
    }
    val ret = String.valueOf(some.toArray)
    ret.equals(ret.reverse)
  }


}
