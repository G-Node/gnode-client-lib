package org.gnode.lib.conf

import org.gnode.lib.client._

object Default {

  val CONFIGURATION = ConfigurationReader.create("ray",
						 "pass",
						 "hal10.g-node.pri",
						 80,
						 "neo",
						 "v1",
						 "MEMORY",
						 "")

  val TRANSFER_MANAGER = new TransferManager(CONFIGURATION)

}