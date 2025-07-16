import model.*;
import rounds.*;

import java.io.*;
import java.util.*;

public class Main {
    private static final String SAVE_FILE = "tournament_state.ser";

    public static void main(String[] args) {
        AdminInputHandler adminInput = new AdminInputHandler();
        BallotHandler ballotHandler = new BallotHandler();
        Scanner scanner = new Scanner(System.in);

        List<Team> teams = new ArrayList<>();
        Map<String, Set<String>> pastMatchups = new HashMap<>();
        PowerMatcher matcher = new PowerMatcher(pastMatchups);
        NominationTracker nominationTracker = new NominationTracker();
        int totalRounds;
        int numJudges;
        int numAwards;
        int roundCounter;

        System.out.println("Welcome to MockScore, the free Mock Trial tabulation software!");

        // Load or new tournament
        System.out.print("Load existing tournament? (y/n): ");
        boolean loadExisting = scanner.nextLine().trim().equalsIgnoreCase("y");
        if (loadExisting && new File(SAVE_FILE).exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
                TournamentState state = (TournamentState) ois.readObject();
                teams = state.teams;
                pastMatchups = state.pastMatchups;
                matcher = new PowerMatcher(pastMatchups);
                nominationTracker = state.nominationTracker;
                totalRounds = state.totalRounds;
                numJudges = state.numJudges;
                numAwards = state.numAwards;
                roundCounter = state.roundCounter;
                System.out.println("✅ Tournament loaded.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("❌ Failed to load tournament. Starting new.");
                return;
            }
        } else {
            System.out.println("You must enter an even number of rounds (2, 4, 6, ...)");
            System.out.print("Enter total number of rounds (must be even): ");
            totalRounds = Integer.parseInt(scanner.nextLine());

            if (totalRounds % 2 != 0) {
                System.out.println("❌ Number of rounds must be even.");
                return;
            }

            while (true) {
                Team team = adminInput.inputTeam();
                teams.add(team);
                System.out.println("✅ Team added. Current count: " + teams.size());

                System.out.print("Add another team? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    break;
                }
            }

            if (teams.size() % 2 != 0) {
                System.out.println("⚠️ Odd number of teams detected. Adding 'BYE BUSTER' dummy team.");
                teams.add(createByeBusterTeam());
            }

            System.out.print("How many judges per round? ");
            numJudges = Integer.parseInt(scanner.nextLine());

            System.out.print("How many outstanding attorney/witness awards? (1, 3, 5, 10, or 15): ");
            numAwards = Integer.parseInt(scanner.nextLine());

            roundCounter = 1;
        }

        while (roundCounter <= totalRounds) {
            System.out.println("\n🌀 Creating Round " + roundCounter + "...");
            List<Matchup> matchups = matcher.createRound(teams, numJudges, totalRounds);

            Round round = new Round();
            round.roundNumber = roundCounter;
            round.matchups = matchups;

            System.out.println("\n✅ Round " + round.roundNumber + " Matchups:");
            for (Matchup m : matchups) {
                System.out.println(" - " + m.plaintiffTeam.id + " (P) vs " + m.defenseTeam.id + " (D) in " + m.courtroomOrZoom);
            }

            for (Matchup m : matchups) {
                m.ballots = new ArrayList<>(Collections.nCopies(numJudges, null));
                for (int j = 0; j < numJudges; j++) {
                    Ballot b = ballotHandler.inputSingleBallot(m.plaintiffTeam, m.defenseTeam);
                    b.calculateResults();
                    m.ballots.set(j, b);

                    b.plaintiffTeam.totalPoints += b.plaintiffPoints;
                    b.defenseTeam.totalPoints += b.defensePoints;
                    b.plaintiffTeam.pointDiff += b.margin;
                    b.defenseTeam.pointDiff -= b.margin;

                    if (b.winner == b.plaintiffTeam) {
                        b.plaintiffTeam.ballotsWon++;
                    } else if (b.winner == b.defenseTeam) {
                        b.defenseTeam.ballotsWon++;
                    }

                    ballotHandler.enterJudgeNominations(numJudges, nominationTracker);

                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
                        TournamentState saveState = new TournamentState(teams, pastMatchups, nominationTracker, totalRounds, numJudges, numAwards, roundCounter);
                        oos.writeObject(saveState);
                        System.out.println("💾 Auto-saved after ballot.");
                    } catch (IOException e) {
                        System.out.println("❌ Failed to save tournament.");
                    }
                }

                boolean t1IsPlaintiff = m.plaintiffTeam != null && !m.plaintiffTeam.id.equals("BYE BUSTER");
                boolean t2IsPlaintiff = m.defenseTeam != null && !m.defenseTeam.id.equals("BYE BUSTER");
                if (t1IsPlaintiff && t2IsPlaintiff) {
                    matcher.recordMatchup(m.plaintiffTeam, m.defenseTeam, true);
                }
            }

            if (roundCounter == totalRounds) {
                System.out.println("All rounds completed.");
                break;
            }

            System.out.print("\nRun next round? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;

            roundCounter++;
        }

        System.out.println("\n🏁 Final Standings:");
        teams.removeIf(t -> t.id.equals("BYE BUSTER"));
        teams.sort(Comparator.comparingInt((Team t) -> t.ballotsWon).reversed()
                .thenComparingInt(t -> t.pointDiff).reversed()
                .thenComparingInt(t -> t.totalPoints).reversed());

        try (FileWriter writer = new FileWriter("tournament_results.txt")) {
            int rank = 1;
            for (Team t : teams) {
                String line = String.format("%d. %s - W:%d, Diff:%d, Points:%d%n",
                        rank++, t.id, t.ballotsWon, t.pointDiff, t.totalPoints);
                System.out.print(line);
                writer.write(line);
            }

            writer.write("\n🏅 Top " + numAwards + " Attorneys:\n");
            printTopAwardWinners(writer, nominationTracker, RoleType.ATTORNEY, numAwards, teams);

            writer.write("\n🎭 Top " + numAwards + " Witnesses:\n");
            printTopAwardWinners(writer, nominationTracker, RoleType.WITNESS, numAwards, teams);

            System.out.println("\nResults exported to tournament_results.txt ✅");
        } catch (IOException e) {
            System.out.println("❌ Failed to write tournament results to file.");
        }

        System.out.println("\n🏅 Final Individual Awards:");
        System.out.println("\nTop " + numAwards + " Attorneys:");
        printTopAwardWinnersConsole(nominationTracker, RoleType.ATTORNEY, numAwards, teams);
        System.out.println("\nTop " + numAwards + " Witnesses:");
        printTopAwardWinnersConsole(nominationTracker, RoleType.WITNESS, numAwards, teams);

        try (PrintWriter csv = new PrintWriter("tournament_results.csv")) {
            csv.println("Rank,Team ID,Ballots Won,Point Differential,Total Points");
            int csvRank = 1;
            for (Team t : teams) {
                csv.printf("%d,%s,%d,%d,%d%n", csvRank++, t.id, t.ballotsWon, t.pointDiff, t.totalPoints);
            }

            csv.println();
            csv.println("Top Attorneys");
            csv.println("Rank,Name,Team,Points");
            NominationTracker finalNominationTracker1 = nominationTracker;
            List<Team> finalTeams1 = teams;
            nominationTracker.getAll().entrySet().stream()
                    .filter(e -> e.getValue().containsKey(RoleType.ATTORNEY))
                    .sorted((a, b) -> Integer.compare(
                            b.getValue().getOrDefault(RoleType.ATTORNEY, 0),
                            a.getValue().getOrDefault(RoleType.ATTORNEY, 0)))
                    .limit(numAwards)
                    .forEachOrdered(entry -> {
                        String team = finalTeams1.stream().filter(t -> t.students.contains(entry.getKey())).map(t -> t.id).findFirst().orElse("Unknown");
                        int rank = finalNominationTracker1.getAll().entrySet().stream()
                                .filter(e -> e.getValue().containsKey(RoleType.ATTORNEY))
                                .sorted((a, b) -> Integer.compare(
                                        b.getValue().getOrDefault(RoleType.ATTORNEY, 0),
                                        a.getValue().getOrDefault(RoleType.ATTORNEY, 0)))
                                .toList().indexOf(entry) + 1;
                        csv.printf("%d,%s,%s,%d%n", rank, entry.getKey(), team, finalNominationTracker1.getPoints(entry.getKey(), RoleType.ATTORNEY));
                    });

            csv.println();
            csv.println("Top Witnesses");
            csv.println("Rank,Name,Team,Points");
            NominationTracker finalNominationTracker = nominationTracker;
            List<Team> finalTeams = teams;
            nominationTracker.getAll().entrySet().stream()
                    .filter(e -> e.getValue().containsKey(RoleType.WITNESS))
                    .sorted((a, b) -> Integer.compare(
                            b.getValue().getOrDefault(RoleType.WITNESS, 0),
                            a.getValue().getOrDefault(RoleType.WITNESS, 0)))
                    .limit(numAwards)
                    .forEachOrdered(entry -> {
                        String team = finalTeams.stream().filter(t -> t.students.contains(entry.getKey())).map(t -> t.id).findFirst().orElse("Unknown");
                        int rank = finalNominationTracker.getAll().entrySet().stream()
                                .filter(e -> e.getValue().containsKey(RoleType.WITNESS))
                                .sorted((a, b) -> Integer.compare(
                                        b.getValue().getOrDefault(RoleType.WITNESS, 0),
                                        a.getValue().getOrDefault(RoleType.WITNESS, 0)))
                                .toList().indexOf(entry) + 1;
                        csv.printf("%d,%s,%s,%d%n", rank, entry.getKey(), team, finalNominationTracker.getPoints(entry.getKey(), RoleType.WITNESS));
                    });

            System.out.println("📄 CSV results exported to tournament_results.csv ✅");
        } catch (IOException e) {
            System.out.println("❌ Failed to write CSV results.");
        }
    }

    private static void printTopAwardWinners(FileWriter writer, NominationTracker tracker, RoleType roleType, int count, List<Team> teams) throws IOException {
        List<Map.Entry<String, EnumMap<RoleType, Integer>>> sorted = tracker.getAll().entrySet().stream()
                .filter(e -> e.getValue().containsKey(roleType))
                .sorted((a, b) -> Integer.compare(
                        b.getValue().getOrDefault(roleType, 0),
                        a.getValue().getOrDefault(roleType, 0)))
                .limit(count)
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            String name = sorted.get(i).getKey();
            String team = teams.stream().filter(t -> t.students.contains(name)).map(t -> t.id).findFirst().orElse("Unknown");
            writer.write((i + 1) + ". " + name + " (" + team + ") - " + tracker.getPoints(name, roleType) + " pts\n");
        }
    }

    private static void printTopAwardWinnersConsole(NominationTracker tracker, RoleType roleType, int count, List<Team> teams) {
        List<Map.Entry<String, EnumMap<RoleType, Integer>>> sorted = tracker.getAll().entrySet().stream()
                .filter(e -> e.getValue().containsKey(roleType))
                .sorted((a, b) -> Integer.compare(
                        b.getValue().getOrDefault(roleType, 0),
                        a.getValue().getOrDefault(roleType, 0)))
                .limit(count)
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            String name = sorted.get(i).getKey();
            String team = teams.stream().filter(t -> t.students.contains(name)).map(t -> t.id).findFirst().orElse("Unknown");
            System.out.println((i + 1) + ". " + name + " (" + team + ") - " + tracker.getPoints(name, roleType) + " pts");
        }
    }

    private static Team createByeBusterTeam() {
        Team bye = new Team();
        bye.id = "BYE BUSTER";
        bye.ballotsWon = 0;
        bye.totalPoints = 0;
        bye.pointDiff = -9999;
        return bye;
    }
}
