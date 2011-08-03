package org.gnode.lib.neo

/** Return type for Connector.getList(type: String). */
case class NEObjectList(selected: List[String],
			object_total: Int,
			object_selected: Int,
			selected_as_of: Int,
			message: String)

/** NEO unit-bearing object */
case class NEOQuant(units: String, data: Double)

/** NEO unit-bearing object (Array) */
case class NEOQuantMult(units: String, data: List[Double]) // TODO: Find *solid* combination.

/** Basis for all primary NEO* case classes. */
sealed abstract class NEObject(neo_id: String)

/** NEO block */
case class NEOBlock(neo_id: String,

		    name: String,
		    author: String,
		    size: Int,

		    index: Int,
		    segment: List[String],
		    filedatetime: String) extends NEObject(neo_id)

/** NEO analogsignal */
case class NEOAnalogSignal(neo_id: String,

			   name: String,
			   author: String,
			   size: Int,

			   t_start: NEOQuant,
			   sampling_rate: NEOQuant,
			   analogsignalarray: Option[List[String]],
			   recordingchannel: Option[List[String]],
			   signal: NEOQuantMult,
			   date_created: String,
			   segment: Option[String]) extends NEObject(neo_id)

/** NEO segment */
case class NEOSegment(neo_id: String,

		      name: String,
		      author: String,
		      size: Int,

		      index: Int,
		      analogsignalarray: List[String],
		      filedatetime: String,
		      eventarray: List[String],
		      epocharray: List[String],
		      epoch: List[String],
		      spiketrain: List[String],
		      spike: List[String],
		      analogsignal: List[String],
		      date_created: String,
		      event: List[String],
		      block: String,
		      irsaanalogsignal: List[String]) extends NEObject(neo_id)

case class NEOEvent(neo_id: String,

		    time: NEOQuant,
		    label: String,

		    author: String,
		    date_created: String,
		    segment: String,
		    eventarray: Option[String],
		    size: Int) extends NEObject(neo_id)

case class NEOEpoch(neo_id: String,

		    time: NEOQuant,
		    duration: NEOQuant,
		    label: String,

		    author: String,
		    epocharray: Option[String],
		    date_created: String,
		    segment: String,
		    size: Int) extends NEObject(neo_id)

case class NEOUnit(neo_id: String,

		   spiketrain: List[String],
		   spike: List[String],
		   recordingchannel: List[String],

		   name: String,
		   author: String,
		   date_created: String,
		   size: Int) extends NEObject(neo_id)

case class NEOWaveform(waveform: NEOQuantMult,
		       time_of_spike: Option[NEOQuant],
		       channel_index: Int)

case class NEOSpikeTrain(neo_id: String,

			 t_start: NEOQuant,
			 t_stop: NEOQuant,
			 times: NEOQuantMult,
			 waveforms: List[NEOWaveform],

			 date_created: String,
			 segment: String,
			 unit: Option[String],
			 size: Int) extends NEObject(neo_id)

case class NEOSpike(neo_id: String,

		    time: NEOQuant,
		    waveforms: List[NEOWaveform],
		    sampling_rate: NEOQuant,
		    left_sweep: NEOQuant,
		    
		    date_created: String,
		    segment: String,
		    unit: Option[String],
		    size: Int) extends NEObject(neo_id)

case class NEORecordingChannel(neo_id: String,

			       index: Int,
			       recordingchannelgroup: String,
			       irsaanalogsignal: List[String],
			       analogsignal: List[String],
			       unit: List[String],

			       date_created: String,
			       size: Int,
			       name: String,
			       author: String) extends NEObject(neo_id)
		      
/*
case class NEOEpochArray extends NEObject(neo_id)
case class NEOAnalogSignalArray extends NEObject(neo_id)
case class NEORecordingChannelGroup extends NEObject(neo_id)
*/
