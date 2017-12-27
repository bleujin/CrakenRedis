package net.bleujin.rcraken;

import java.util.stream.Stream;

public class WriteStream extends AbstractStream<WriteNode, WriteStream> implements Stream<WriteNode> {

	private WriteSession wsession ;
	
	WriteStream(WriteSession wsession, Stream<WriteNode> stream) {
		super(stream) ;
		this.wsession = wsession ;
	}

}
