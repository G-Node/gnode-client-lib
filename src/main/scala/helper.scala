/* Copyright (C) 2011 by German Neuroinformatics Node
 * www.g-node.org

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */

package org.gnode.lib.util

import dispatch._
import dispatch.mime.Mime._
import dispatch.liftjson.Js._
import net.liftweb.json._

/** Utility object. Provides file management support (e.g., auto-closing of
 * resources). */

object Network {

  def check() = try {
    val h = new Http with NoLogging
    h(url("http://www.google.com") >|)
    h.shutdown()
    true
  } catch {
    case StatusCode(_,_) => false
    case _ => false
  }

  def uploadFile(h: Http, file_location: String, remote_location: Request, name: String): String = {
    // This function takes a local and remote location and uploads the former to
    // the latter with name "name". Furthermore, it extracts the permalink returned
    // by the server.

    import java.io.File
    val local_file = new File(file_location)
    
    h(remote_location <<* (name, local_file) ># { json => (json \ "selected" \ "permalink") match {
      case JString(d) =>
	"/datafiles/" + d.split("/").last + "/"
      case _ => ""
    }})
  
  }

  def downloadFile(h: Http, location: String, prefix: String = "ephys", suffix: String = ".h5", dir: String = ""): String = {
    // This function takes a URL, downloads the file synchronously to a
    // temporary location, and results in a path to the retrieved
    // file.

    import java.io.{FileOutputStream,File}

    // Generate temporary path:
    val tmp = if (dir.isEmpty) {
      File.createTempFile(prefix, suffix)
    } else {
      val dirf = new File(dir)
      File.createTempFile(prefix, suffix, dirf)
    }

    h(url(location) >>> new FileOutputStream(tmp))

    tmp.getPath

  }

  def downloadFileNoAuth(location: String, prefix: String = "ephys", suffix: String = ".hd5", dir: String = ""): String =
    downloadFile(new Http, location, prefix, suffix, dir)

}

object IDExtractor {

  def extractID(url: String): String = {
    val s = url.split("/")
    s.slice(s.length - 2, s.length).reduceLeft(_ + "_" + _)
  }

  def implodeID(relData: Map[String, Array[String]]) =
    (for ((key, list) <- relData) yield (key, list.map(_.split("_").last.toInt))).toMap

}

object FileUtil {

  /** Helper function. Offers auto-close functionality for close()-providing
   * resources. */

  def using[T <: { def close() }, R](resource: T)(block: T => R): R = {
    
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }

  }

}
