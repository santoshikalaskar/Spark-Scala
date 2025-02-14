package WordCountQuestion

import DateTimeGenerator.GenerateDateTime
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, SparkSession}
import org.apache.spark.sql.functions.{explode, split}
import org.apache.spark.SparkContext

class WordCount {

  private def getDateTime():String = {
    val generateDateTimeObj = new GenerateDateTime()
    val currentTime = generateDateTimeObj.generateTimeDDMMYYFormat()
    currentTime
  }

  def countWordsRDD(sparkContext: SparkContext,inputFile:String,outputPath:String):Map[String, Int] =  {
    try {
      val rawData = sparkContext.textFile(inputFile)
      val words = rawData.flatMap(line => line.split(" "))
      val wordsKv = words.map(word => (word, 1))
      val outputRDD = wordsKv.reduceByKey(_ + _)
      println(outputRDD.collect().toMap)
      val currentTime = getDateTime()
      outputRDD.saveAsTextFile(outputPath+currentTime)
      outputRDD.collect().toMap
    }
    catch {
      case _:org.apache.hadoop.mapred.InvalidInputException => throw new WordCountException(WordCountExceptionEnum.mapRIOException)
    }
  }

  def countWordsDataFrame(spark:SparkSession,inputFile:String,outputPath:String):DataFrame = {
    try {
      val textDf = spark.read.text(inputFile)
      val wordsDf = textDf.select(explode(split(textDf("value")," ")).alias("word"))
      val countDf = wordsDf.groupBy("word").count()
      countDf.show(10)
      val currentDateTime = getDateTime()
      countDf.write.csv(outputPath+currentDateTime)
      countDf
    }
    catch {
      case _:org.apache.spark.sql.AnalysisException => throw new WordCountException(WordCountExceptionEnum.sparkSqlException)
    }
  }

  def countWordsDataset(spark:SparkSession,inputFile:String,outputPath:String):DataFrame = {

    try {
      import spark.implicits._
      val textDataSet = spark.read.text(inputFile).as[String]
      val wordsDataset = textDataSet.flatMap(_.split(" ")).withColumnRenamed("value","words")
      val countDF = wordsDataset.groupBy("words").count()
      countDF.show(10)
      val currentDateTime = getDateTime()
      countDF.write.csv(outputPath+currentDateTime)
      countDF
    }
    catch {
      case _:org.apache.spark.sql.AnalysisException => throw new WordCountException(WordCountExceptionEnum.sparkSqlException)
    }
  }
}





