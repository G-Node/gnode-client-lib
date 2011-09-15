package org.gnode.lib.conf

import org.gnode.lib.conf.ConfigurationReader
import org.gnode.lib.conf.Configuration

object Defaults {

  val DEFAULT_CONFIGURATION = ConfigurationReader.create("ray",
							 "pass",
							 "hal10.g-node.pri")

}
