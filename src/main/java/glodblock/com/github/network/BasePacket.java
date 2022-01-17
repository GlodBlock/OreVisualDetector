package glodblock.com.github.network;

/**
 * Created by wital_000 on 20.03.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public interface BasePacket {

    int getPacketID();

    byte[] encode();

   void process();

}
