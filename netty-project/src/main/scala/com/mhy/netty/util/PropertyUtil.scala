package com.mhy.netty.util

import java.io.{File, FileFilter}
import java.util.{NoSuchElementException, Properties}

import com.mhy.netty.util.ConfigProvider


/**
  * Created by root on 16-8-9.
  */
class PropertyUtil(val file:String) extends ConfigProvider{
  private var propFile : String = file
  override def get(name: String): String = {
    val ret =  getProp(name)
    if(ret == null){
      throw new NoSuchElementException;
    }
    return ret;
  }

  private var prop:Properties = null
  def getProp(key:String):String={
    if(prop == null){
      prop = new Properties()
      val f = PropertyUtil.getClass.getClassLoader.getResourceAsStream(propFile)
      prop.load(f)
    }
    return prop.getProperty(key)
  }
}

object PropertyUtil{

  def main(args: Array[String]) {
  }
}
