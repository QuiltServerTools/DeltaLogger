package com.github.fabricservertools.deltalogger.network;

import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.beans.Transaction;
import com.github.fabricservertools.deltalogger.beans.TransactionPos;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;

public class NetworkUtils {
    public static final Identifier PLACEMENT_PACKET = new Identifier("deltalogger", "placement");
    public static final Identifier TRANSACTION_PACKET = new Identifier("deltalogger", "transaction");
    public static PacketByteBuf setTransaction(Transaction transaction, BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeString(transaction.getPlayerName());
        buf.writeLong(SQLUtils.getInstantFromDBTimeString(transaction.getTime()).getEpochSecond());
        buf.writeInt(transaction.getCount());
        buf.writeString(transaction.getItemType());

        return buf;
    }

    public static PacketByteBuf setTransaction(TransactionPos transactionPos) {
        return setTransaction(new Transaction(0, transactionPos.getPlayerName(), transactionPos.getTime(), transactionPos.getItemType(), transactionPos.getCount(), Util.NIL_UUID, null), transactionPos.getPos());
    }

    public static PacketByteBuf setPlacement(Placement placement) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(placement.getX());
        buf.writeInt(placement.getY());
        buf.writeInt(placement.getZ());
        buf.writeBoolean(placement.getPlaced());
        buf.writeString(placement.getPlayerName());
        buf.writeLong(SQLUtils.getInstantFromDBTimeString(placement.getTime()).getEpochSecond());
        buf.writeString(placement.getBlockType());
        buf.writeString(placement.getState() == null ? "" : placement.getState());

        return buf;
    }
}
