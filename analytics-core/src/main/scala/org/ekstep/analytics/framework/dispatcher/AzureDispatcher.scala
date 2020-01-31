package org.ekstep.analytics.framework.dispatcher

import java.io.FileWriter
import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.ekstep.analytics.framework.exception.DispatcherException
import org.ekstep.analytics.framework.util.{CommonUtil, JobLogger}
import org.sunbird.cloud.storage.conf.AppConf
import org.sunbird.cloud.storage.factory.{StorageConfig, StorageServiceFactory}
import org.ekstep.analytics.framework.Level
import scala.concurrent.Await
import org.ekstep.analytics.framework.FrameworkContext
import org.apache.hadoop.fs.FileUtil
import org.apache.hadoop.fs.FileSystem
import java.net.URI
import org.apache.hadoop.fs.Path
import org.ekstep.analytics.framework.util.JSONUtils

object AzureDispatcher extends HadoopDispatcher with IDispatcher {

    implicit val className = "org.ekstep.analytics.framework.dispatcher.AzureDispatcher"

    override def dispatch(config: Map[String, AnyRef], events: RDD[String])(implicit sc: SparkContext, fc: FrameworkContext) = {

        val bucket = config.getOrElse("bucket", null).asInstanceOf[String];
        val key = config.getOrElse("key", null).asInstanceOf[String];
        val isPublic = config.getOrElse("public", false).asInstanceOf[Boolean];
        
        if (null == bucket || null == key) {
            throw new DispatcherException("'bucket' & 'key' parameters are required to send output to azure")
        }
        
        val srcFile = getAzureFile(bucket, "_tmp/" + key);
        val destFile = getAzureFile(bucket, key);
        
        dispatch(srcFile, destFile, sc.hadoopConfiguration, events)
    }

    def getAzureFile(bucket: String, file: String) : String = {
      "wasb://" + bucket + "@" + AppConf.getStorageKey(AppConf.getStorageType()) + ".blob.core.windows.net/" + file;
    }

}
