package alchemystar.engine.net.response.jdbc;

import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.net.proto.util.Versions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class SelectIncrementResponse {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("@@session.auto_increment_increment", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer = header.writeBuf(buffer, ctx);
        for (FieldPacket field : fields) {
            buffer = field.writeBuf(buffer, ctx);
        }
        buffer = eof.writeBuf(buffer, ctx);
        byte packetId = eof.packetId;
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add("1".getBytes());
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);
        ctx.writeAndFlush(buffer);
    }
}
