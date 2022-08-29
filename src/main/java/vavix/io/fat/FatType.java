/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import vavi.util.ByteUtil;
import vavi.util.Debug;
import vavi.util.StringUtil;

import vavix.io.IOSource;


/**
 * FatType.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
public enum FatType implements Fat {
    /**
     * <li>0000_0000h               unused cluster
     * <li>0000_0002h ~ 0fff_fff6h  next cluster for a file
     * <li>0fff_fff7h               bad cluster
     * <li>0fff_fff8h ~ 0fff_ffffh  last cluster for a file (used 0fff_ffffh normally)
     */
    Fat32Fat(32) {
        /** */
        private static final long serialVersionUID = -8008307331300948383L;
        @Override
        protected int nextCluster(int cluster) throws IOException {
            int sector = getFatSector() + (cluster * 4) / bpb.getBytesPerSector();
            if (sector != currentSector) {
                io.readSector(buffer, sector);
                currentSector = sector;
            }
            int position = (cluster * 4) % bpb.getBytesPerSector();
//Debug.println(Level.FINE, "sector: " + sector + "\n" + StringUtil.getDump(buffer, position, 8));
            int nextCluster = ByteUtil.readLeInt(buffer, position);
//Debug.printf(Level.FINE, "cluster: %1$d, sector: %2$d, position: %3$d, %3$08x, next: %4$d%n", cluster, sector, position, nextCluster);
            return nextCluster;
        }

        @Override
        public Integer[] getClusterChain(int cluster) throws IOException {
            List<Integer> clusters = new ArrayList<>();
            do {
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                clusters.add(cluster);
                cluster = nextCluster(cluster);
            } while (0x0000_0002 <= cluster && cluster <= 0x0fff_fff6);
            return clusters.toArray(new Integer[0]);
        }

        @Override
        public boolean isUsing(int cluster) throws IOException {
            cluster = nextCluster(cluster);
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
            return 0x0000_0002 <= cluster && cluster <= 0x0fff_ffff;
        }
    },
    Fat16Fat(16) {
        /** */
        private static final long serialVersionUID = 1846975683407080677L;

        @Override
        protected int nextCluster(int cluster) throws IOException {
            int sector = getFatSector() + (cluster * 2) / bpb.getBytesPerSector();
            if (sector != currentSector) {
                io.readSector(buffer, sector);
                currentSector = sector;
            }
            int position = (cluster * 2) % bpb.getBytesPerSector();
Debug.println(Level.FINE, "sector: " + sector + "\n" + StringUtil.getDump(buffer, position, 8));
            int nextCluster = ByteUtil.readLeShort(buffer, position) & 0xffff;
Debug.printf(Level.FINE, "cluster: %1$d, sector: %2$d, position: %3$d, %3$08x, next: %4$d%n", cluster, sector, position, (nextCluster & 0xfff8) > 0 ? -1 : nextCluster);
            return nextCluster;
        }

        @Override
        public Integer[] getClusterChain(int cluster) throws IOException {
            List<Integer> clusters = new ArrayList<>();
            do {
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                clusters.add(cluster);
                cluster = nextCluster(cluster);
            } while (0x0002 <= cluster && cluster <= 0xfff6);
            return clusters.toArray(new Integer[0]);
        }

        @Override
        public boolean isUsing(int cluster) throws IOException {
            cluster = nextCluster(cluster);
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
            return 0x0002 <= cluster && cluster <= 0xffff;
        }
    },
    Fat12Fat(12) {
        /** */
        private static final long serialVersionUID = -3706950356198032944L;

        @Override
        protected int nextCluster(int cluster) throws IOException {
            int sector = getFatSector() + (cluster + cluster / 2) / bpb.getBytesPerSector();
            if (sector != currentSector) {
                io.readSector(buffer, sector);
                currentSector = sector;
            }
            int position = (cluster + cluster / 2) % bpb.getBytesPerSector();
//Debug.println(Level.FINE, "sector: " + sector + "\n" + StringUtil.getDump(buffer, position, 8));
            int nextCluster = (ByteUtil.readLeShort(buffer, position) >> ((cluster & 1) != 0 ? 4 : 0)) & 0x0fff;
//Debug.printf(Level.FINE, "cluster: %1$d, sector: %2$d, position: %3$d, %3$08x, next: %4$d%n", cluster, sector, position, nextCluster);
            return nextCluster;
        }

        @Override
        public Integer[] getClusterChain(int cluster) throws IOException {
            List<Integer> clusters = new ArrayList<>();
            do {
//Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                clusters.add(cluster);
                cluster = nextCluster(cluster);
            } while (0x002 <= cluster && cluster <= 0xff6);
            return clusters.toArray(new Integer[0]);
        }

        @Override
        public boolean isUsing(int cluster) throws IOException {
            cluster = nextCluster(cluster);
//Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
            return 0x002 <= cluster && cluster <= 0xfff;
        }
    };

    protected IOSource io;

    protected BiosParameterBlock bpb;

    public void setRuntimeContext(IOSource io, BiosParameterBlock bpb) {
        this.io = io;
        this.bpb = bpb;
    }

    /** */
    private int fatNumber = 0;

    /** */
    public void setFatNumber(int fatNumber) {
        this.fatNumber = fatNumber;
    }

    /** */
    protected int getFatSector() {
         return bpb.getFatStartSector(fatNumber);
    }

    /** TODO thread unsafe */
    protected byte[] buffer = new byte[1024];

    /** TODO thread unsafe */
    protected int currentSector = -1;

    /**
     * TODO thread unsafe
     * @param cluster cluster
     */
    protected abstract int nextCluster(int cluster) throws IOException;

    /** */
    public void setClusterValue(int cluster, int value) throws IOException {
        throw new UnsupportedOperationException("read only, use #useUserFat()");
    }

    /** TODO thread unsafe */
    public int getClusterValue(int cluster) throws IOException {
        return nextCluster(cluster);
    }

    /** */
    public void setClusterChain(Integer[] clusters) throws IOException {
        throw new UnsupportedOperationException("read only, use #useUserFat()");
    }

    /** fat bits */
    final int value;

    FatType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

/* */
