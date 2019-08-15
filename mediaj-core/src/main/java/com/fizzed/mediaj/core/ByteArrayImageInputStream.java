/*
 * Extracted from 12Monkies project since I only need this one file. Adapted
 * a tad to remove dependencies on 12Monkies lang and common libs.
 *
 * Copyright (c) 2008, Harald Kuhr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fizzed.mediaj.core;

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;

public final class ByteArrayImageInputStream extends ImageInputStreamImpl {
    private final byte[] data;
    private final int dataOffset;
    private final int dataLength;

    public ByteArrayImageInputStream(final byte[] pData) {
        this(pData, 0, pData != null ? pData.length : -1);
    }

    public ByteArrayImageInputStream(final byte[] pData, int offset, int length) {
        data = pData;
        dataOffset = offset;
        dataLength = length;
    }

    @Override
    public int read() throws IOException {
        if (streamPos >= dataLength) {
            return -1;
        }

        bitOffset = 0;

        return data[((int) streamPos++) + dataOffset] & 0xff;
    }

    @Override
    public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        if (streamPos >= dataLength) {
            return -1;
        }

        int length = (int) Math.min(this.dataLength - streamPos, pLength);
        bitOffset = 0;
        System.arraycopy(data, (int) streamPos + dataOffset, pBuffer, pOffset, length);
        streamPos += length;

        return length;
    }

    @Override
    public long length() {
        return dataLength;
    }

    @Override
    public boolean isCached() {
        return true;
    }

    @Override
    public boolean isCachedMemory() {
        return true;
    }
}