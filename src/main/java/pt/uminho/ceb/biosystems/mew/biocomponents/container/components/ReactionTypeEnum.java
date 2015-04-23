package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

public enum ReactionTypeEnum {
	
	Drain{
		@Override
		public String getDescription(){
			return "DRAIN";
		}
	},
	
	Transport{
		@Override
		public String getDescription(){
			return "TRANSPORT";
		}
	},
	
	Undefined{
		@Override
		public String getDescription(){
			return "UNDEFINED";
		}
	},
	
	Biomass{
		@Override
		public String getDescription(){
			return "BIOMASS";
		}
	},
	
	Internal{
		@Override
		public String getDescription(){
			return "INTERNAL";
		}
	};

	public abstract String getDescription();
}
