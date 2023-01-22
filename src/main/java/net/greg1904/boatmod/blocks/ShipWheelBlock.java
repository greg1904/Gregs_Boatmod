package net.greg1904.boatmod.blocks;

import net.greg1904.boatmod.util.BlockAndPos;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShipWheelBlock extends HorizontalFacingBlock {
    private boolean isEntity = false;
    private static final int BOAT_SIZE_LIMIT = 50;

    private List<BlockAndPos> shipBlocks = Collections.synchronizedList(new ArrayList<>());
    private BlockPos wheelPos;
    private boolean isExiting = false;


    public ShipWheelBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        switch(dir) {
            case NORTH:
                return VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
            case SOUTH:
                return VoxelShapes.cuboid(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
            case EAST:
                return VoxelShapes.cuboid(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            case WEST:
                return VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
            default:
                return VoxelShapes.fullCube();
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(!world.isClient){
            player.sendMessage(Text.of("Starting BoatBuild"), false);
            findBoatBlocks(world, pos, player);
            if(isEntity == true)
                player.sendMessage(Text.of("Ending BoatBuild"), false);
        }

        return ActionResult.SUCCESS;
    }

    private void findBoatBlocks(World world, BlockPos pos, PlayerEntity player){
        wheelPos = pos;
        BlockPos posBelow = wheelPos.down();

        try {
            isExiting = false;
            findBoatBlocksRec(player, world, posBelow, 0, 0, 0, 0, 0, 0);
            isEntity = true;
            for(BlockAndPos bp : shipBlocks){
                world.setBlockState(bp.pos(), Blocks.PINK_WOOL.getDefaultState());
            }
        }catch (RuntimeException | InterruptedException e){
            player.sendMessage(Text.of("Creation Failed: " + e.getMessage()), false);
        }
    }

    private void findBoatBlocksRec(PlayerEntity player, World world, BlockPos currentBlockPos, int stepsDown, int stepsNorth, int stepsEast, int stepsWest, int stepsSouth, int stepsUp) throws RuntimeException, InterruptedException {
        if(isExiting)
            return;

        if (stepsDown > BOAT_SIZE_LIMIT || stepsNorth > BOAT_SIZE_LIMIT ||
                stepsEast > BOAT_SIZE_LIMIT || stepsWest > BOAT_SIZE_LIMIT ||
                stepsSouth > BOAT_SIZE_LIMIT || stepsUp > BOAT_SIZE_LIMIT){
            isExiting = true;
            throw new RuntimeException("Size Limit Reached");
        }

        Block block = world.getBlockState(currentBlockPos).getBlock();

        if (block.getTranslationKey().equals("block.minecraft.bedrock")){
            isExiting = true;
            throw new RuntimeException("Bedrock found - Landmass attached");
        }

        if (!checkBlockIsBorder(block)){
            player.sendMessage(Text.of(new StringBuilder("Block: ")
                    .append(block.getTranslationKey()).append("-").append(stepsDown).append(stepsNorth)
                    .append(stepsEast).append(stepsWest).append(stepsSouth).append(stepsUp).toString()));
                    shipBlocks.add(new BlockAndPos(block, currentBlockPos));

//            if (stepsDown + stepsNorth + stepsEast + stepsWest + stepsSouth + stepsUp == 0){
//                ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
//                executor.submit(()-> {
//                    try {
//                        findBoatBlocksRec(world, currentBlockPos.down(), stepsDown+1, stepsNorth, stepsEast, stepsWest, stepsSouth, stepsUp);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Boat Creation Interrupted");
//                    }
//                });
//                executor.submit(()-> {
//                    try {
//                        findBoatBlocksRec(world, currentBlockPos.north(), stepsDown, stepsNorth+1, stepsEast, stepsWest, stepsSouth, stepsUp);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Boat Creation Interrupted");
//                    }
//                });
//                executor.submit(()-> {
//                    try {
//                        findBoatBlocksRec(world, currentBlockPos.east(), stepsDown, stepsNorth, stepsEast+1, stepsWest, stepsSouth, stepsUp);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Boat Creation Interrupted");
//                    }
//                });
//                executor.submit(()-> {
//                    try {
//                        findBoatBlocksRec(world, currentBlockPos.west(), stepsDown, stepsNorth, stepsEast, stepsWest+1, stepsSouth, stepsUp);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Boat Creation Interrupted");
//                    }
//                });
//                executor.submit(()-> {
//                    try {
//                        findBoatBlocksRec(world, currentBlockPos.south(), stepsDown, stepsNorth, stepsEast, stepsWest, stepsSouth+1, stepsUp);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Boat Creation Interrupted");
//                    }
//                });
//                executor.submit(()-> {
//                    try {
//                        findBoatBlocksRec(world, currentBlockPos.up(), stepsDown, stepsNorth, stepsEast, stepsWest, stepsSouth, stepsUp+1);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Boat Creation Interrupted");
//                    }
//                });
//                executor.awaitTermination(2, TimeUnit.MINUTES);
//            }else{
                findBoatBlocksRec(player, world, currentBlockPos.down(), stepsDown+1, stepsNorth, stepsEast, stepsWest, stepsSouth, stepsUp);
                findBoatBlocksRec(player, world, currentBlockPos.north(), stepsDown, stepsNorth+1, stepsEast, stepsWest, stepsSouth, stepsUp);
                findBoatBlocksRec(player, world, currentBlockPos.east(), stepsDown, stepsNorth, stepsEast+1, stepsWest, stepsSouth, stepsUp);
                findBoatBlocksRec(player, world, currentBlockPos.west(), stepsDown, stepsNorth, stepsEast, stepsWest+1, stepsSouth, stepsUp);
                findBoatBlocksRec(player, world, currentBlockPos.south(), stepsDown, stepsNorth, stepsEast, stepsWest, stepsSouth+1, stepsUp);
                findBoatBlocksRec(player, world, currentBlockPos.up(), stepsDown, stepsNorth, stepsEast, stepsWest, stepsSouth, stepsUp+1);
//            }
        }
    }

    private boolean checkBlockIsBorder(Block block){
        switch (block.getTranslationKey()){
            case "block.minecraft.air":
            case "block.minecraft.lava":
            case "block.minecraft.water":
                return true;
            default:
                return false;
        }
    }
}
