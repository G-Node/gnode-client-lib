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

package org.gnode.lib.matlab

import org.gnode.lib.api._

// Collection of MATLAB-specific utilities that facilitate interfacing
// between JVM and, crucially, Scala-specific functionality that isn't
// covered by Mathworks' helper libraries.

object Helper {

  // Certain edge cases require us to check for None-ness from
  // MATLAB. This function does that and nothing else:
  
  def isNone[T](that: Option[T]) =
    that match {
      case None => true
      case _ => false
    }

  // MATLAB's string handling is pretty bad, so this does a simple
  // string split in order to get "analogsignal" from
  // "analogsignal_1".

  def guessType(id: String) = {
    val splitter = new APIHelper {}
    val parts = splitter.split(id)

    if (parts._1.isEmpty) throw new Exception("Could not approximate object type")
    else parts._1
  }

}
