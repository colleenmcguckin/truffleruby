/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.format.unpack;

import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.format.FormatNode;
import org.jruby.truffle.core.format.SharedTreeBuilder;
import org.jruby.truffle.core.format.SourceNode;
import org.jruby.truffle.core.format.control.AtUnpackNode;
import org.jruby.truffle.core.format.control.BackUnpackNode;
import org.jruby.truffle.core.format.control.ForwardUnpackNode;
import org.jruby.truffle.core.format.control.NNode;
import org.jruby.truffle.core.format.control.SequenceNode;
import org.jruby.truffle.core.format.decode.DecodeByteNodeGen;
import org.jruby.truffle.core.format.decode.DecodeFloat32NodeGen;
import org.jruby.truffle.core.format.decode.DecodeFloat64NodeGen;
import org.jruby.truffle.core.format.decode.DecodeInteger16BigNodeGen;
import org.jruby.truffle.core.format.decode.DecodeInteger16LittleNodeGen;
import org.jruby.truffle.core.format.decode.DecodeInteger32BigNodeGen;
import org.jruby.truffle.core.format.decode.DecodeInteger32LittleNodeGen;
import org.jruby.truffle.core.format.decode.DecodeInteger64BigNodeGen;
import org.jruby.truffle.core.format.decode.DecodeInteger64LittleNodeGen;
import org.jruby.truffle.core.format.read.ReadBERNodeGen;
import org.jruby.truffle.core.format.read.ReadBase64StringNodeGen;
import org.jruby.truffle.core.format.read.ReadBinaryStringNodeGen;
import org.jruby.truffle.core.format.read.ReadBitStringNodeGen;
import org.jruby.truffle.core.format.read.ReadByteNodeGen;
import org.jruby.truffle.core.format.read.ReadBytesNodeGen;
import org.jruby.truffle.core.format.read.ReadHexStringNodeGen;
import org.jruby.truffle.core.format.read.ReadMIMEStringNodeGen;
import org.jruby.truffle.core.format.read.ReadUTF8CharacterNodeGen;
import org.jruby.truffle.core.format.read.ReadUUStringNodeGen;
import org.jruby.truffle.core.format.type.AsUnsignedNodeGen;
import org.jruby.truffle.core.format.write.WriteValueNodeGen;
import org.jruby.truffle.core.format.pack.PackBaseListener;
import org.jruby.truffle.core.format.pack.PackParser;
import org.jruby.truffle.language.control.RaiseException;

import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class UnpackTreeBuilder extends PackBaseListener {

    private final RubyContext context;
    private final Node currentNode;

    private final SharedTreeBuilder sharedTreeBuilder;

    private final Deque<List<FormatNode>> sequenceStack = new ArrayDeque<>();

    public UnpackTreeBuilder(RubyContext context, Node currentNode) {
        this.context = context;
        this.currentNode = currentNode;
        sharedTreeBuilder = new SharedTreeBuilder(context);
        pushSequence();
    }

    @Override
    public void enterSequence(PackParser.SequenceContext ctx) {
        pushSequence();
    }

    @Override
    public void exitSequence(PackParser.SequenceContext ctx) {
        final List<FormatNode> sequence = sequenceStack.pop();
        appendNode(new SequenceNode(context, sequence.toArray(new FormatNode[sequence.size()])));
    }

    @Override
    public void exitInt8(PackParser.Int8Context ctx) {
        appendNode(sharedTreeBuilder.applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        DecodeByteNodeGen.create(context, true,
                                ReadByteNodeGen.create(context,
                                        new SourceNode())))));
    }

    @Override
    public void exitUint8(PackParser.Uint8Context ctx) {
        appendNode(sharedTreeBuilder.applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        DecodeByteNodeGen.create(context, false,
                                ReadByteNodeGen.create(context,
                                        new SourceNode())))));
    }

    @Override
    public void exitInt16Little(PackParser.Int16LittleContext ctx) {
        appendIntegerNode(16, ByteOrder.LITTLE_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt16Big(PackParser.Int16BigContext ctx) {
        appendIntegerNode(16, ByteOrder.BIG_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt16Native(PackParser.Int16NativeContext ctx) {
        appendIntegerNode(16, ByteOrder.nativeOrder(), ctx.count(), true);
    }

    @Override
    public void exitUint16Little(PackParser.Uint16LittleContext ctx) {
        appendIntegerNode(16, ByteOrder.LITTLE_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint16Big(PackParser.Uint16BigContext ctx) {
        appendIntegerNode(16, ByteOrder.BIG_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint16Native(PackParser.Uint16NativeContext ctx) {
        appendIntegerNode(16, ByteOrder.nativeOrder(), ctx.count(), false);
    }

    @Override
    public void exitInt32Little(PackParser.Int32LittleContext ctx) {
        appendIntegerNode(32, ByteOrder.LITTLE_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt32Big(PackParser.Int32BigContext ctx) {
        appendIntegerNode(32, ByteOrder.BIG_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt32Native(PackParser.Int32NativeContext ctx) {
        appendIntegerNode(32, ByteOrder.nativeOrder(), ctx.count(), true);
    }

    @Override
    public void exitUint32Little(PackParser.Uint32LittleContext ctx) {
        appendIntegerNode(32, ByteOrder.LITTLE_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint32Big(PackParser.Uint32BigContext ctx) {
        appendIntegerNode(32, ByteOrder.BIG_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint32Native(PackParser.Uint32NativeContext ctx) {
        appendIntegerNode(32, ByteOrder.nativeOrder(), ctx.count(), false);
    }

    @Override
    public void exitInt64Little(PackParser.Int64LittleContext ctx) {
        appendIntegerNode(64, ByteOrder.LITTLE_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt64Big(PackParser.Int64BigContext ctx) {
        appendIntegerNode(64, ByteOrder.BIG_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt64Native(PackParser.Int64NativeContext ctx) {
        appendIntegerNode(64, ByteOrder.nativeOrder(), ctx.count(), true);
    }

    @Override
    public void exitUint64Little(PackParser.Uint64LittleContext ctx) {
        appendIntegerNode(64, ByteOrder.LITTLE_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint64Big(PackParser.Uint64BigContext ctx) {
        appendIntegerNode(64, ByteOrder.BIG_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint64Native(PackParser.Uint64NativeContext ctx) {
        appendIntegerNode(64, ByteOrder.nativeOrder(), ctx.count(), false);
    }

    @Override
    public void exitUtf8Character(PackParser.Utf8CharacterContext ctx) {
        appendNode(sharedTreeBuilder.applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        ReadUTF8CharacterNodeGen.create(context,
                                new SourceNode()))));
    }

    @Override
    public void exitBerInteger(PackParser.BerIntegerContext ctx) {
        appendNode(sharedTreeBuilder.applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        ReadBERNodeGen.create(context,
                                new SourceNode()))));
    }

    @Override
    public void exitF64Native(PackParser.F64NativeContext ctx) {
        appendFloatNode(64, ByteOrder.nativeOrder(), ctx.count());
    }

    @Override
    public void exitF32Native(PackParser.F32NativeContext ctx) {
        appendFloatNode(32, ByteOrder.nativeOrder(), ctx.count());
    }

    @Override
    public void exitF64Little(PackParser.F64LittleContext ctx) {
        appendFloatNode(64, ByteOrder.LITTLE_ENDIAN, ctx.count());
    }

    @Override
    public void exitF32Little(PackParser.F32LittleContext ctx) {
        appendFloatNode(32, ByteOrder.LITTLE_ENDIAN, ctx.count());
    }

    @Override
    public void exitF64Big(PackParser.F64BigContext ctx) {
        appendFloatNode(64, ByteOrder.BIG_ENDIAN, ctx.count());
    }

    @Override
    public void exitF32Big(PackParser.F32BigContext ctx) {
        appendFloatNode(32, ByteOrder.BIG_ENDIAN, ctx.count());
    }

    @Override
    public void exitBinaryStringSpacePadded(PackParser.BinaryStringSpacePaddedContext ctx) {
        final SourceNode source = new SourceNode();
        final FormatNode readNode;

        if (ctx.count() == null) {
            readNode = ReadBinaryStringNodeGen.create(context, false, false, 1, true, true, false, source);
        } else if (ctx.count().INT() == null) {
            readNode = ReadBinaryStringNodeGen.create(context, true, false, -1, true, true, false, source);
        } else {
            final int count = Integer.parseInt(ctx.count().INT().getSymbol().getText());
            readNode = ReadBinaryStringNodeGen.create(context, false, false, count, true, true, false, source);
        }

        appendNode(WriteValueNodeGen.create(context, readNode));
    }

    @Override
    public void exitBinaryStringNullPadded(PackParser.BinaryStringNullPaddedContext ctx) {
        final SourceNode source = new SourceNode();
        final FormatNode readNode;

        if (ctx.count() == null) {
            readNode = ReadBinaryStringNodeGen.create(context, false, false, 1, false, false, false, source);
        } else if (ctx.count().INT() == null) {
            readNode = ReadBinaryStringNodeGen.create(context, true, false, -1, false, false, false, source);
        } else {
            final int count = Integer.parseInt(ctx.count().INT().getSymbol().getText());
            readNode = ReadBinaryStringNodeGen.create(context, false, false, count, false, false, false, source);
        }

        appendNode(WriteValueNodeGen.create(context, readNode));
    }

    @Override
    public void exitBinaryStringNullStar(PackParser.BinaryStringNullStarContext ctx) {
        final SourceNode source = new SourceNode();
        final FormatNode readNode;

        if (ctx.count() == null) {
            readNode = ReadBinaryStringNodeGen.create(context, false, true, 1, false, true, true, source);
        } else if (ctx.count().INT() == null) {
            readNode = ReadBinaryStringNodeGen.create(context, true, true, -1, false, true, true, source);
        } else {
            final int count = Integer.parseInt(ctx.count().INT().getSymbol().getText());
            readNode = ReadBinaryStringNodeGen.create(context, false, false, count, false, true, true, source);
        }

        appendNode(WriteValueNodeGen.create(context, readNode));
    }

    @Override
    public void exitBitStringMSBFirst(PackParser.BitStringMSBFirstContext ctx) {
        bitString(ByteOrder.BIG_ENDIAN, ctx.count());
    }

    @Override
    public void exitBitStringMSBLast(PackParser.BitStringMSBLastContext ctx) {
        bitString(ByteOrder.LITTLE_ENDIAN, ctx.count());
    }

    @Override
    public void exitHexStringHighFirst(PackParser.HexStringHighFirstContext ctx) {
        hexString(ByteOrder.BIG_ENDIAN, ctx.count());
    }

    @Override
    public void exitHexStringLowFirst(PackParser.HexStringLowFirstContext ctx) {
        hexString(ByteOrder.LITTLE_ENDIAN, ctx.count());
    }

    @Override
    public void exitUuString(PackParser.UuStringContext ctx) {
        appendNode(
                WriteValueNodeGen.create(context,
                        ReadUUStringNodeGen.create(context,
                                new SourceNode())));
    }

    @Override
    public void exitMimeString(PackParser.MimeStringContext ctx) {
        appendNode(WriteValueNodeGen.create(context,
                ReadMIMEStringNodeGen.create(context, new SourceNode())));
    }

    @Override
    public void exitBase64String(PackParser.Base64StringContext ctx) {
        appendNode(WriteValueNodeGen.create(context,
                ReadBase64StringNodeGen.create(context, new SourceNode())));
    }

    @Override
    public void exitAt(PackParser.AtContext ctx) {
        final int position;

        if (ctx.count() == null) {
            position = 0;
        } else if (ctx.count() != null && ctx.count().INT() == null) {
            return;
        } else {
            position = Integer.parseInt(ctx.count().INT().getText());
        }

        appendNode(new AtUnpackNode(context, position));
    }

    @Override
    public void exitBack(PackParser.BackContext ctx) {
        if (ctx.count() != null && ctx.count().INT() == null) {
            appendNode(new BackUnpackNode(context, true));
        } else if (ctx.count() == null || ctx.count().INT() != null) {
            appendNode(sharedTreeBuilder.applyCount(ctx.count(), new BackUnpackNode(context, false)));
        }
    }

    @Override
    public void exitNullByte(PackParser.NullByteContext ctx) {
        if (ctx.count() != null && ctx.count().INT() == null) {
            appendNode(new ForwardUnpackNode(context, true));
        } else if (ctx.count() == null || ctx.count().INT() != null) {
            appendNode(sharedTreeBuilder.applyCount(ctx.count(), new ForwardUnpackNode(context, false)));
        }
    }

    @Override
    public void enterSubSequence(PackParser.SubSequenceContext ctx) {
        pushSequence();
    }

    @Override
    public void exitSubSequence(PackParser.SubSequenceContext ctx) {
        final List<FormatNode> sequence = sequenceStack.pop();
        final SequenceNode sequenceNode = new SequenceNode(context, sequence.toArray(new FormatNode[sequence.size()]));

        final FormatNode resultingNode;

        if (ctx.INT() == null) {
            resultingNode = sequenceNode;
        } else {
            resultingNode = new NNode(context, Integer.parseInt(ctx.INT().getText()), sequenceNode);
        }

        appendNode(resultingNode);
    }

    @Override
    public void exitErrorDisallowedNative(PackParser.ErrorDisallowedNativeContext ctx) {
        throw new RaiseException(context.getCoreLibrary().argumentError(
                "'" + ctx.NATIVE().getText() + "' allowed only after types sSiIlLqQ", currentNode));
    }

    public FormatNode getNode() {
        return sequenceStack.peek().get(0);
    }

    private void pushSequence() {
        sequenceStack.push(new ArrayList<FormatNode>());
    }

    private void appendNode(FormatNode node) {
        sequenceStack.peek().add(node);
    }

    private boolean consumePartial(PackParser.CountContext ctx) {
        return ctx != null && ctx.INT() == null;
    }

    private void appendIntegerNode(int size, ByteOrder byteOrder, PackParser.CountContext count, boolean signed) {
        final FormatNode readNode = ReadBytesNodeGen.create(context, size / 8, consumePartial(count), new SourceNode());
        final FormatNode convertNode = createIntegerDecodeNode(size, byteOrder, signed, readNode);
        appendNode(sharedTreeBuilder.applyCount(count, WriteValueNodeGen.create(context, convertNode)));
    }

    private void appendFloatNode(int size, ByteOrder byteOrder, PackParser.CountContext count) {
        final FormatNode readNode = readBytesAsInteger(size, byteOrder, consumePartial(count), true);
        final FormatNode decodeNode;

        switch (size) {
            case 32:
                decodeNode = DecodeFloat32NodeGen.create(context, readNode);
                break;
            case 64:
                decodeNode = DecodeFloat64NodeGen.create(context, readNode);
                break;
            default:
                throw new IllegalArgumentException();
        }

        final FormatNode writeNode = WriteValueNodeGen.create(context, decodeNode);
        appendNode(sharedTreeBuilder.applyCount(count, writeNode));
    }

    private FormatNode readBytesAsInteger(int size, ByteOrder byteOrder, boolean consumePartial, boolean signed) {
        final FormatNode readNode = ReadBytesNodeGen.create(context, size / 8, consumePartial, new SourceNode());
        return createIntegerDecodeNode(size, byteOrder, signed, readNode);
    }

    private FormatNode createIntegerDecodeNode(int size, ByteOrder byteOrder, boolean signed, FormatNode readNode) {
        FormatNode decodeNode;

        switch (size) {
            case 16:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    decodeNode = DecodeInteger16LittleNodeGen.create(context, readNode);
                } else {
                    decodeNode = DecodeInteger16BigNodeGen.create(context, readNode);
                }
                break;
            case 32:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    decodeNode = DecodeInteger32LittleNodeGen.create(context, readNode);
                } else {
                    decodeNode = DecodeInteger32BigNodeGen.create(context, readNode);
                }
                break;
            case 64:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    decodeNode = DecodeInteger64LittleNodeGen.create(context, readNode);
                } else {
                    decodeNode = DecodeInteger64BigNodeGen.create(context, readNode);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (!signed) {
            decodeNode = AsUnsignedNodeGen.create(context, decodeNode);
        }

        return decodeNode;
    }

    private void bitString(ByteOrder byteOrder, PackParser.CountContext ctx) {
        final SharedTreeBuilder.StarLength starLength = sharedTreeBuilder.parseCountContext(ctx);

        appendNode(WriteValueNodeGen.create(context,
                ReadBitStringNodeGen.create(context, byteOrder, starLength.isStar(), starLength.getLength(),
                        new SourceNode())));
    }

    private void hexString(ByteOrder byteOrder, PackParser.CountContext ctx) {
        final SharedTreeBuilder.StarLength starLength = sharedTreeBuilder.parseCountContext(ctx);

        appendNode(WriteValueNodeGen.create(context,
                ReadHexStringNodeGen.create(context, byteOrder, starLength.isStar(), starLength.getLength(),
                        new SourceNode())));

    }

}
