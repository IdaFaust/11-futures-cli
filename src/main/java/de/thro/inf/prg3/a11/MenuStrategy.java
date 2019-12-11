package de.thro.inf.prg3.a11;

import de.thro.inf.prg3.a11.openmensa.OpenMensaAPI;
import de.thro.inf.prg3.a11.openmensa.OpenMensaAPIService;
import de.thro.inf.prg3.a11.openmensa.model.Canteen;
import de.thro.inf.prg3.a11.openmensa.model.Meal;
import de.thro.inf.prg3.a11.openmensa.model.PageInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum MenuStrategy {

	SHOW_CANTEENS {
		@Override
		void execute() {
			System.out.print("Fetching canteens [");
			/* TODO fetch all canteens and print them to STDOUT
			 * at first get a page without an index to be able to extract the required pagination information
			 * afterwards you can iterate the remaining pages
			 * keep in mind that you should await the process as the user has to select canteen with a specific id */
			openMensaAPI.getCanteens().thenApply(response -> {
				System.out.println("list mit kantienen erstellt");
				List<Canteen> canteenList;
				PageInfo pageInfo = PageInfo.extractFromResponse(response);

				if(response.body() == null){
					canteenList = new LinkedList<Canteen>();
				}else{
					canteenList = response.body();
				}

				for(int i = 2; i < pageInfo.getTotalCountOfPages(); i++ ){
					try {
						canteenList.addAll(openMensaAPI.getCanteens(i).get());
					}catch (Exception e){
						System.out.println("Ooops: " + e.toString());
					}
				}
				return canteenList;
			}).thenApply(canteenList -> {
				System.out.println("vorherige liste sortieren nach id");
				canteenList.sort(Comparator.comparing(canteen -> canteen.getId()));
				return canteenList;
			}).thenAccept(canteenList -> {
				System.out.println("ausgabe aller kantienen");
				for(Canteen canteen : canteenList){
					System.out.println("Canteen: " + canteen);
				}
			});
		}
	},
	SET_CANTEEN {
		@Override
		void execute() {
			readCanteen();
		}
	},
	SHOW_MEALS {
		@Override
		void execute() {
			/* TODO fetch all meals for the currently selected canteen
			 * to avoid errors retrieve at first the state of the canteen and check if the canteen is opened at the selected day
			 * don't forget to check if a canteen was selected previously! */
			openMensaAPI.getCanteenState(currentCanteenId, dateFormat.format(currentDate.getTime())).thenApply(state -> {
				if (state != null && !state.isClosed()){
					return openMensaAPI.getMeals(currentCanteenId, dateFormat.format(currentDate.getTime()));
				}else{
					List<Meal> mealListEmpty = new LinkedList<Meal>();
					return mealListEmpty;
				}
			});

		}
	},
	SET_DATE {
		@Override
		void execute() {
			readDate();
		}
	},
	QUIT {
		@Override
		void execute() {
			System.exit(0);
		}
	};

	abstract void execute();

	private static final String OPEN_MENSA_DATE_FORMAT = "yyyy-MM-dd";

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(OPEN_MENSA_DATE_FORMAT, Locale.getDefault());
	private static final Scanner inputScanner = new Scanner(System.in);
	private static final OpenMensaAPI openMensaAPI = OpenMensaAPIService.getInstance().getOpenMensaAPI();
	private static final Calendar currentDate = Calendar.getInstance();
	private static int currentCanteenId = -1;

	/**
	 * Utility method to select a canteen
	 */
	protected static void readCanteen() {
		/* typical input reading pattern */
		boolean readCanteenId = false;
		do {
			try {
				System.out.println("Enter canteen id:");
				currentCanteenId = inputScanner.nextInt();
				readCanteenId = true;
			} catch (Exception e) {
				System.out.println("Sorry could not read the canteen id");
			}
		} while (!readCanteenId);
	}

	/**
	 * Utility method to read a date and update the calendar
	 */
	protected static void readDate() {
		/* typical input reading pattern */
		boolean readDate = false;
		do {
			try {
				System.out.println("Please enter date in the format yyyy-mm-dd:");
				Date d = dateFormat.parse(inputScanner.next());
				currentDate.setTime(d);
				readDate = true;
			} catch (ParseException p) {
				System.out.println("Sorry, the entered date could not be parsed.");
			}
		} while (!readDate);

	}
}
