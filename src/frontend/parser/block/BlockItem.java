package frontend.parser.block;

public class BlockItem {
    private final BlockItemEle blockItemEle;

    public BlockItem(BlockItemEle blockItemEle) {
        this.blockItemEle = blockItemEle;
    }

    public BlockItemEle getBlockItemEle() {
        return blockItemEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(blockItemEle.toString());
        return sb.toString();
    }
}
