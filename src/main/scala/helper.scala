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

// This package provides a number of utility functions, such as simplified
// binary file downloading, network connectivity checks, file utilities,
// etc.

object Network {

  // Simple, non-stateful connectivity check. We create a fresh
  // Dispatch HTTP object (sans logging!), call the supplied location,
  // using a regular GET, and check if it errors out. This can be
  // improved. For instance, currently, a 404 or 500 yields false.
  
  def check(loc: String) = try {
    val h = new Http with NoLogging
    h(url(loc) >|)
    h.shutdown()
    true
  } catch {
    case StatusCode(_,_) => false
    case _ => false
  }

  def uploadFile(h: Http, file_location: String, remote_location: Request, name: String): String = {

    // This function takes one local and one remote location and uploads the former to
    // the latter with name "name". Furthermore, it extracts the permalink returned
    // by the server. This is highly specific, but I'm not sure how to make this extraction
    // more generic (e.g., in requirements.json?).
    //
    // There's a case to be made that this should be part of the CONNECTION MANAGER,
    // and not a utility method; may be refactored.

    import java.io.File
    val local_file = new File(file_location)
    
    h(remote_location <<* (name, local_file) ># { json => (json \ "selected" \ "permalink") match {
      case JString(d) => d
      case _ => ""
    }})
  
  }

  def downloadFileCache(h: Http, location: String, local_location: String, etag: String, prefix: String = "ephys", suffix: String = ".h5", dir: String = ""): Array[String] = {

    // Cache-enabled version of the fully state-free downloadFile(). Logic is simple and
    // equivalent to all cache-based download methods: We supply the server with the locally
    // stored ETAG, check for status code, and either download (if 200), or retrieve
    // from disk (if 304). This function has no access to the underlying cache register.
    // Everything is explicitly SUPPLIED.
    
    import java.io.{FileOutputStream,File}

    val tmp = if (dir.isEmpty) {
      File.createTempFile(prefix, suffix)
    } else {
      val dirf = new File(dir)
      File.createTempFile(prefix, suffix, dirf)
    }

    // Critical cache logic:
    val headers = Map("If-None-Match" -> etag)

    val request = url(location) <:< headers
    val out = new FileOutputStream(tmp)
    val handler = request >+ { req =>
      (req >>> out, req >:> { _("ETag") }) }

    try {

      // 200: Object new or modified
      val (_, new_etag) = h(handler)
      out.close

      // We now return the LOCAL PATH to the file, as well as the new ETAG:
      return Array(tmp.getPath, new_etag.head)

    } catch {

      // Cacheable
      case StatusCode(304, _) =>
	out.close
	return Array(local_location, etag)
      case StatusCode(_, msg) =>
	out.close
	return Array(msg, etag)

    }

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

    // This handles the actual download. There's a lot of room for
    // enhancement here -- it's a fully synchronous call without
    // the opportunity for chunked streaming, progress updates, or
    // detailed network monitoring.
    
    h(url(location) >>> new FileOutputStream(tmp))

    tmp.getPath

  }

  // Cheap wrapper for downloadFile() in case we don't have to supply an authenticated HTTP
  // session that is already available; primarily a debug helper.
  
  def downloadFileNoAuth(location: String, prefix: String = "ephys", suffix: String = ".h5", dir: String = ""): String =
    downloadFile(new Http, location, prefix, suffix, dir)

}

object IDExtractor {

  // URL helper utility.
  
  def extractID(url: String): String = {
    val s = url.split("/")
    s.slice(s.length - 2, s.length).reduceLeft(_ + "_" + _)
  }

  def implodeID(relData: Map[String, Array[String]]) =
    (for ((key, list) <- relData) yield (key, list.map(_.split("_").last.toInt))).toMap

}

object FileUtil {

  // Nifty little tool for auto-closing resource objects.

  def using[T <: { def close() }, R](resource: T)(block: T => R): R = {
    
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }

  }

}
