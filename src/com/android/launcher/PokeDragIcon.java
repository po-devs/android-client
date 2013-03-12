package com.android.launcher;

import com.pokebros.android.pokemononline.BattleActivity;
import com.pokebros.android.pokemononline.poke.BattlePoke;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class PokeDragIcon extends ImageView implements DragSource, DropTarget {
	boolean isTarget = false;
	public int[] otherIconDim = new int[4];
	public BattleActivity battleActivity;
	public int num;
	
	public PokeDragIcon(Context context) {
		super(context);
	}

	public PokeDragIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PokeDragIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		if(success) {
			
			TranslateAnimation otherTa = new TranslateAnimation(0f, getLeft() - target.getLeft(), -1*(target.getHeight() + 10), 0f);
			otherTa.setDuration(200);
			target.startAnimation(otherTa);
		
			PokeDragIcon otherIcon = (PokeDragIcon)target;
			otherIcon.otherIconDim[0] = this.getLeft();
			otherIcon.otherIconDim[1] = this.getTop();
			otherIcon.otherIconDim[2] = this.getRight();
			otherIcon.otherIconDim[3] = this.getBottom();
			otherIcon.isTarget = true;
			
			BattlePoke temp = battleActivity.activeBattle.myTeam.pokes[num];
			battleActivity.activeBattle.myTeam.pokes[num] = battleActivity.activeBattle.myTeam.pokes[otherIcon.num];
			battleActivity.activeBattle.myTeam.pokes[otherIcon.num] = temp;
			
			int tempNum = num;
			num = otherIcon.num;
			otherIcon.num = tempNum;
			
			System.out.println(battleActivity.activeBattle.myTeam.pokes[0].nick + " IS IN FRONT");
			
			this.layout(target.getLeft(), target.getTop(), target.getRight(), target.getBottom());
		}
	}

	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		//Accept all things by default
		return true;
	}

	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		TranslateAnimation ta = new TranslateAnimation(0f, 0f, 0f, -1*(getHeight() + 10));
		ta.setDuration(100);
		ta.setFillAfter(true);
		startAnimation(ta);
		
	}

	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		TranslateAnimation ta = new TranslateAnimation(0f, 0f, -1*(getHeight() + 10), 0f);
		ta.setDuration(100);
		startAnimation(ta);
	}

	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub

	}

	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAnimationEnd() {
		if(isTarget) {
			this.layout(otherIconDim[0], otherIconDim[1], otherIconDim[2], otherIconDim[3]);
			isTarget = false;
		}
	}
}
