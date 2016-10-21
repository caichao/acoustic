package hust.cc.acoustic.communication;

import java.io.IOException;

/**
 * Created by Administrator on 2016/10/21.
 */

public interface ICommService {
   void send(short[] data) throws IOException;
    void close() throws  IOException;
}
