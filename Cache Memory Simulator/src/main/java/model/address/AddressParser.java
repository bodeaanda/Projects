package model.address;

public class AddressParser {
    private int blockSize;
    private int nrSets;

    public AddressParser(int blockSize, int nrSets) {
        this.blockSize = blockSize;
        this.nrSets = nrSets;
    }
    public long getBlockOffset(long address) {
        return address % blockSize;
    }
    public int getSetIndex(long address) {
        return (int) ((address / blockSize) % nrSets);
    }
    public long getTag(long address) {
        return (address / blockSize) / nrSets;
    }
}
