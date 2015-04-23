/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

import java.io.Serializable;

public class ReactionConstraintCI implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;
	
	public static final double INFINITY = 10000;
	
	
	protected double upperLimit;
	protected double lowerLimit;
	
	public ReactionConstraintCI(){
		upperLimit = -INFINITY;
		lowerLimit = INFINITY;
	}
	
	public ReactionConstraintCI(double lowerLimit, double upperLimit){
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}
	
	public ReactionConstraintCI(ReactionConstraintCI reactionConstraint) {
		this.lowerLimit = reactionConstraint.lowerLimit;
		this.upperLimit = reactionConstraint.upperLimit;
	}
	
	public double getLowerLimit(){
		return lowerLimit;
	}
	
	public double getUpperLimit(){
		return upperLimit;
	}

	public void setUpperLimit(double upperLimit){
		this.upperLimit = upperLimit;
	}
	
	public void setLowerLimit(double lowerLimit){
		this.lowerLimit = lowerLimit;
	}
	
	@Override
	public ReactionConstraintCI clone() {
		return new ReactionConstraintCI(this);
	}
	
	public String toString(){
		return "["+lowerLimit + ", " + upperLimit + "]";
	}
}
