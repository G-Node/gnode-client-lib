package org.gnode.lib.neo

/** Return type for Connector.getList(type: String). */
case class NEObjectList(selected: List[String],
			object_total: Int,
			object_selected: Int,
			selected_as_of: Int,
			message: String)

/** Basis for all NEO* case classes. */
sealed abstract class NEObject(neo_id: String)

/** NEO block */
case class NEOBlock(neo_id: String,
		    name: String,
		    author: String,
		    segment: List[String],
		    size: Int,
		    filedatetime: String,
		    index: Int)
     extends NEObject(neo_id)
/*
case class NEOSegment extends NEObject(neo_id)
case class NEOEvent extends NEObject(neo_id)
case class NEOEpoch extends NEObject(neo_id)
case class NEOEpochArray extends NEObject(neo_id)
case class NEOUnit extends NEObject(neo_id)
case class NEOSpikeTrain extends NEObject(neo_id)
case class NEOAnalogSignal extends NEObject(neo_id)
case class NEOAnalogSignalArray extends NEObject(neo_id)
case class NEOSpike extends NEObject(neo_id)
case class NEORecordingChannelGroup extends NEObject(neo_id)
case class NEORecordingChannel extends NEObject(neo_id)
*/
