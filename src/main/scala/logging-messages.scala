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

package org.gnode.lib.conf

object LogMessages {

  // org.gnode.lib.client.HttpInteractor
  def HTTP_SHUTDOWN = "Shutting down"
  
  // org.gnode.lib.client.Authenticator
  def AUTHENTICATE_BEGIN(user: String) = "Performing authentication for user %s".format(user)
  def AUTHENTICATE_FAILURE(user: String) = "Authentication failure for %s".format(user)
  def AUTHENTICATE_ERROR(user: String) = "Nonspecific error while authenticating %s".format(user)
  
  // General errors
  def HTTP_GENERAL(code: Int, message: String) = "General HTTP error (%d): %s".format(code, message)
  def FILE_NOT_FOUND(file: String) = "File@%s could not be found".format(file)
  
  // org.gnode.lib.client.Downloader
  def RETRIEVE_LIST_START(t: String) = "Retrieving list for type %s".format(t)
  def RETRIEVE_LIST_SUCCESS(t: String) = "Successfull list build (%s)".format(t)

  def RETRIEVE_LIST_ERROR_PARSE(t: String) = "Problem while parsing object list (%s)".format(t)
  def RETRIEVE_LIST_ERROR_404(t: String) = "Object type %s doesn't exist".format(t)
  def RETRIEVE_LIST_ERROR_GENERIC(t: String) = "Generic error while retrieving %s list".format(t)

  def RETRIEVE_OBJECT_ERROR_404(id: String) = "Object %s was not found".format(id)
  def RETRIEVE_OBJECT_ERROR_NOT_AUTHORISED(id: String) = "No authorisation to access %s".format(id)
  def RETRIEVE_OBJECT_ERROR_GENERIC(id: String) = "Unknown error while retrieving %s".format(id)

  def CACHE_HIT(id: String, tag: String) = "Cache hit for %s@%s".format(id, tag)
  def CACHE_TRY(id: String, tag: String) = "Disconnected. Trying cache for %s@%s".format(id, tag)
  def CACHE_MISS(id: String, tag: String) = "Cache miss for %s@%s".format(id, tag)

  def JOB_ADD(id: String) = "Enqueued new job: %s".format(id)
  def JOB_COMPLETE(id: String) = "Successfully completed job: %s".format(id)
  def JOB_FAILURE(id: String) = "Failed to complete job: %s".format(id)

  def PARSE_ERROR(id: String) = "Could not parse %s".format(id)

  // org.gnode.lib.client.Uploader
  def UPLOAD_VALIDATION_ERROR = "Object didn't pass validation. Not added to upload queue"
  def UPLOAD_NEW_BAD_REQUEST(objectType: String) = "New object with type %s was rejected by remote".format(objectType)
  def UPLOAD_UPDATE_BAD_REQUEST(id: String) = "Update for %s was rejected by remote".format(id)

  // org.gnode.lib.parse.{Reader, Writer}
  def READ_ERROR_PARSE = "Parse exception occurred"
  def READ_ERROR_UNKNOWN = "Unknown error occurred while parsing"

}