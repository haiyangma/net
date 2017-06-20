package scala.schema.test

/**
  * Created by mhy on 2017/6/20.
  */
class Test {

  def fac(n:Int):Int = {
    if (n <= 0){
      1
    }else{
      n * fac(n - 1)
    }
  }

  val bean = new Bean
  def recursiveSum(args : Int*):Int={
    if(args.length == 0) return 0
    else args.head + recursiveSum(args.tail:_*)
  }
}
object Test{

}
