package org.gnode.lib.neo

sealed abstract class NEObject(neo_id: String)

case class NEOBlock(neo_id: String,
		    name: String,
		    author: String,
		    segment: List[String],
		    size: Int,
		    filedatetime: String,
		    index: Int)
     extends NEObject(neo_id)

case class NEObjectList(selected: List[String],
			object_total: Int,
			object_selected: Int,
			selected_as_of: Int,
			message: Option[String])
