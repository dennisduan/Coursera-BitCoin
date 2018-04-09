import java.util.ArrayList;
import java.util.HashMap;


// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.


public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private ArrayList<Block> blocks;
    private TransactionPool txPool;
    
    private HashMap<byte[], Integer> blockHeightMap; 
    private HashMap<byte[], UTXOPool> blockUTXOPool;
    
    private int maxheight;
    
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
    	//
    	blocks = new ArrayList<Block>();
    	txPool = new TransactionPool();
    	blockHeightMap = new HashMap<byte[], Integer>();
    	blockUTXOPool = new HashMap<byte[], UTXOPool>();
    	
    	// Add coinbase of the genesisBlock
    	UTXOPool p = new UTXOPool();
    	Transaction tx = genesisBlock.getCoinbase();
    	UTXO utxo = new UTXO(tx.getHash(), 0);
    	p.addUTXO(utxo, tx.getOutput(0));
    	
    	blocks.add(genesisBlock);
    	maxheight = 1;
    	blockHeightMap.put(genesisBlock.getHash(), maxheight);
    	blockUTXOPool.put(genesisBlock.getHash(), p);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
    	for(Block b: blocks) {
    		if(blockHeightMap.get(b.getHash()) == maxheight)
    			return b;
    	}
    	return null;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
    	return getBlockUTXOPool(getMaxHeightBlock().getHash());
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
    	return txPool;
    }
    
    private UTXOPool getBlockUTXOPool(byte[] hash) {
    	return new UTXOPool(blockUTXOPool.get(hash));
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
    	// If the block is valid: 
    	// 1, All transactions are valid; 2, Block should be at height > (maxheight - CUT_OFF_AGE)
    	ArrayList<Transaction> txs = block.getTransactions();
    	
    	byte[] preBlockHash = block.getPrevBlockHash();
    	Block parent = null;
    	int parentHeight = -1;
    	
    	UTXOPool currentPool;
    	
    	if(block == null || block.getHash() == null)
    		return false;
    	
    	for(Block b : blocks) {
    		if(b.getHash().equals(preBlockHash)) {
    			parent = b;
    		}
    	}
    	
    	if(parent == null) 
    		return false;  	
    	else {
    		UTXOPool prePool = getBlockUTXOPool(preBlockHash);
    		
    		parentHeight = blockHeightMap.get(parent.getHash());
    		
    		if(maxheight - parentHeight > CUT_OFF_AGE)
    			return false;
    		else {
    			TxHandler txhandler = new TxHandler(prePool);
    			
    	    	ArrayList<Transaction> blockTxs = block.getTransactions();
    	    	Transaction[] validTxs = txhandler.handleTxs(blockTxs.toArray(new Transaction[blockTxs.size()]));
    	    	if(validTxs.length != blockTxs.size())
    	    		return false;
    	    	
    	    	// Add coinbase to UTXOPool
    	    	currentPool = txhandler.getUTXOPool();
    	    	Transaction coinbase = block.getCoinbase();
    	    	UTXO utxo = new UTXO(coinbase.getHash(), 0);
    	    	currentPool.addUTXO(utxo, coinbase.getOutput(0));
    		}
    	}
    	
    	blocks.add(block);
    	blockHeightMap.put(block.getHash(), parentHeight+1);
    	blockUTXOPool.put(block.getHash(), new UTXOPool(currentPool));
    	
    	if(parentHeight == maxheight) {
    		maxheight += 1;
    	}
    	
    	return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
    	txPool.addTransaction(tx);
    }
}