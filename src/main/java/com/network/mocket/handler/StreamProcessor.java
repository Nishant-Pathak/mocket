package com.network.mocket.handler;

import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class StreamProcessor {
  private static final Logger LOGGER = Logger.getLogger( StreamProcessor.class.getName() );
  private final List<MocketStreamHandler> handlers;

  public StreamProcessor(List<MocketStreamHandler> streamHandler) {
    this.handlers = streamHandler;
    if (handlers.size() == 0) {
      handlers.add(0, new DefaultHandler());
    }
  }

  public Object decode(Object data) throws ParseException {
    try {
      for (MocketStreamHandler mocketStreamhandler : handlers) {
        data = mocketStreamhandler.decode((byte[]) data);
      }
    } catch (Exception ex) {
      LOGGER.log(Level.FINEST, "failed while decode {0}", ex);
      throw ex;
    }
    return data;
  }

  public Object encode(Object data) throws ParseException {
    try {
      for (MocketStreamHandler mocketStreamhandler : handlers) {
        data = mocketStreamhandler.encode(data);
      }
    } catch (Exception ex) {
      LOGGER.log(Level.FINEST, "failed while encode {0}", ex);
      throw ex;
    }

    return data;
  }
}
