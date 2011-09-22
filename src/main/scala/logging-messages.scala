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

  // org.gnode.lib.parse.{Reader, Writer}
  def READ_ERROR_PARSE = "Parse exception occurred"
  def READ_ERROR_UNKNOWN = "Unknown error occurred while parsing"

}