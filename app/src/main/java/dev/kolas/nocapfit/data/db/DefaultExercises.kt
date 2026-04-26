@file:Suppress("MatchingDeclarationName")

package dev.kolas.nocapfit.data.db

data class DefaultExerciseData(
    val name: String,
    val description: String,
    val tags: String,
)

val DEFAULT_EXERCISES: List<DefaultExerciseData> = listOf(
    // region Chest (1-15, 101)
    DefaultExerciseData(
        name = "Barbell Bench Press",
        description = """
            **Setup:** Lie on a flat bench with eyes directly under the bar. Grip the bar slightly wider than shoulder-width.

            **Execution:** Lower the bar slowly to your mid-chest, tucking your elbows at a 45° angle. Press the bar back up until arms are locked.

            **Pro-Tip:** Keep your feet flat on the floor and squeeze your shoulder blades together to create a stable base.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Dumbbell Bench Press",
        description = """
            **Setup:** Sit on a bench with weights on your knees. Lie back, bringing the weights to your chest.

            **Execution:** Press the dumbbells toward the ceiling until arms are straight. Lower them until the weights are level with your chest.

            **Pro-Tip:** Unlike the barbell, this allows for a deeper range of motion; don't be afraid to let the weights go slightly lower than your chest.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Incline Barbell Press",
        description = """
            **Setup:** Set a bench to a 30-45° angle. Grip the bar as you would for a flat bench.

            **Execution:** Lower the bar to your upper chest (just below the collarbone). Press upward in a slight arc.

            **Pro-Tip:** This targets the "upper" chest; avoid bouncing the bar off your chest to maintain tension.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Anterior Deltoids, Triceps",
    ),
    DefaultExerciseData(
        name = "Incline Dumbbell Press",
        description = """
            **Setup:** Lean back on an incline bench with dumbbells at shoulder height, palms facing forward.

            **Execution:** Drive the weights up until they nearly touch at the top. Lower them slowly to the sides of your upper chest.

            **Pro-Tip:** Keep your wrists stacked directly over your elbows throughout the entire movement.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Anterior Deltoids, Triceps",
    ),
    DefaultExerciseData(
        name = "Decline Barbell Press",
        description = """
            **Setup:** Secure your feet in a decline bench. Lie back and grip the bar with a standard bench grip.

            **Execution:** Lower the bar to your lower pectorals (near the bottom of the ribs). Press back up to the starting position.

            **Pro-Tip:** This is great for the lower chest, but have a spotter—it's harder to "dump" the bar if you fail.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Dumbbell Flys",
        description = """
            **Setup:** Lie flat on a bench with dumbbells held directly above your chest, palms facing each other.

            **Execution:** With a slight bend in the elbows, lower your arms out to the sides in a wide arc. Squeeze your chest to bring them back together.

            **Pro-Tip:** Imagine you are hugging a giant tree; don't let the weights drop below shoulder level.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Cable Crossover",
        description = """
            **Setup:** Stand between two high pulleys with handles. Step forward to create tension.

            **Execution:** Pull the handles down and together in front of your waist, crossing one hand over the other slightly.

            **Pro-Tip:** Focus on the "squeeze" at the bottom rather than using momentum to swing the weights.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Push-up",
        description = """
            **Setup:** Start in a plank position with hands slightly wider than shoulders and a straight line from head to heels.

            **Execution:** Lower your chest until it nearly touches the floor. Push back up, fully extending your arms.

            **Pro-Tip:** Don't let your hips sag; keep your core and glutes tight to protect your lower back.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids, Core",
    ),
    DefaultExerciseData(
        name = "Diamond Push-up",
        description = """
            **Setup:** Get into a push-up position but place your hands together so your index fingers and thumbs form a diamond.

            **Execution:** Lower your chest to your hands, keeping your elbows tucked close to your ribs. Push back up.

            **Pro-Tip:** This is a tricep killer; if it's too hard, start with your knees on the ground.
        """.trimIndent(),
        tags = "Chest, Triceps, Pectoralis Major, Core",
    ),
    DefaultExerciseData(
        name = "Chest Dip",
        description = """
            **Setup:** Grip parallel bars and lift yourself up. Lean your torso forward about 30°.

            **Execution:** Bend your elbows to lower your body until you feel a stretch in your chest. Push back up to the top.

            **Pro-Tip:** If you stay completely upright, you'll target the triceps; the forward lean is what hits the chest.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Machine Chest Press",
        description = """
            **Setup:** Adjust the seat so the handles are at mid-chest height. Sit back firmly against the pad.

            **Execution:** Push the handles forward until arms are extended but not locked. Return slowly to the start.

            **Pro-Tip:** This is perfect for "mechanical drop sets"—going to failure safely without a spotter.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Pec Deck Machine",
        description = """
            **Setup:** Sit with your back against the pad. Place your forearms on the pads or grip the handles (depending on the model).

            **Execution:** Squeeze the handles together in front of your face. Hold the squeeze for a second before slowly opening back up.

            **Pro-Tip:** Don't let the weights touch the stack between reps to keep the muscle under constant tension.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Floor Press",
        description = """
            **Setup:** Lie on the floor with your knees bent. Hold dumbbells or a barbell above your chest.

            **Execution:** Lower the weights until your triceps touch the floor. Pause for a second, then press back up.

            **Pro-Tip:** This is a "partial range" movement that helps build massive power in the lockout phase of the bench press.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Incline Push-up",
        description = """
            **Setup:** Place your hands on a bench or sturdy table and step your feet back into a plank.

            **Execution:** Lower your chest to the edge of the bench. Push back up to the starting position.

            **Pro-Tip:** The higher the surface, the easier the exercise. Use this to build up to standard push-ups.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Triceps, Core",
    ),
    DefaultExerciseData(
        name = "Dumbbell Pullover",
        description = """
            **Setup:** Lie across a bench (perpendicularly) with only your upper back supported. Hold one dumbbell with both hands over your chest.

            **Execution:** Lower the weight behind your head while keeping a slight bend in your elbows. Pull it back up to your chest.

            **Pro-Tip:** This works both the chest and the lats; drop your hips slightly as you lower the weight to increase the stretch.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Latissimus Dorsi, Triceps, Serratus Anterior",
    ),
    DefaultExerciseData(
        name = "Machine Chest Fly",
        description = """
            **Setup:** Adjust the seat height so that the handles or pads are level with your mid-chest. Sit with your back and head firmly against the backrest and plant your feet flat on the floor.

            **Execution:** Grab the handles with a slight bend in your elbows. Squeeze your chest to bring the handles together in a wide arc until they meet in front of you. Pause for a second, then slowly return to the starting position.

            **Pro-Tip:** Focus on the "mind-muscle connection" by imagining you are hugging a large barrel; do not let the weights touch the stack between reps to keep constant tension.
        """.trimIndent(),
        tags = "Chest, Pectoralis Major, Anterior Deltoids",
    ),
    // endregion

    // region Back (16-30, 104)
    DefaultExerciseData(
        name = "Barbell Deadlift",
        description = """
            **Setup:** Stand with feet hip-width apart, shins an inch from the bar. Hinge at the hips to grip the bar.

            **Execution:** Keeping a flat back, drive through your heels to stand up, pulling the bar up your shins. Lock out by squeezing your glutes.

            **Pro-Tip:** Think of it as "pushing the floor away" rather than "pulling the bar up."
        """.trimIndent(),
        tags = "Back, Erector Spinae, Gluteus Maximus, Hamstrings, Traps, Lats, Forearms",
    ),
    DefaultExerciseData(
        name = "Pull-up",
        description = """
            **Setup:** Grip a pull-up bar with an overhand grip, slightly wider than shoulders.

            **Execution:** Pull your chest toward the bar by driving your elbows down. Lower yourself back to a full dead hang.

            **Pro-Tip:** Look slightly upward; this helps engage the lats and prevents you from "rounding" into the bar.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Biceps, Rhomboids, Traps",
    ),
    DefaultExerciseData(
        name = "Chin-up",
        description = """
            **Setup:** Grip the bar with an underhand grip (palms facing you), shoulder-width apart.

            **Execution:** Pull yourself up until your chin clears the bar. Lower back down with control.

            **Pro-Tip:** This targets the biceps more than pull-ups but is still an incredible back builder.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Biceps, Rhomboids, Traps",
    ),
    DefaultExerciseData(
        name = "Lat Pulldown",
        description = """
            **Setup:** Sit at the machine and secure your thighs under the pads. Grip the wide bar with an overhand grip.

            **Execution:** Pull the bar down to your upper chest while leaning back slightly. Squeeze your shoulder blades.

            **Pro-Tip:** Don't pull the bar behind your neck; it puts unnecessary strain on your shoulder joints.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Biceps, Traps, Rhomboids",
    ),
    DefaultExerciseData(
        name = "Seated Cable Row",
        description = """
            **Setup:** Sit with feet on the platforms and knees slightly bent. Grip the handle and sit upright.

            **Execution:** Pull the handle toward your stomach, pulling your elbows back as far as possible. Return with a full stretch.

            **Pro-Tip:** Don't swing your torso back and forth. Keep your back stationary to isolate the muscles.
        """.trimIndent(),
        tags = "Back, Rhomboids, Traps, Latissimus Dorsi, Biceps, Forearms",
    ),
    DefaultExerciseData(
        name = "Bent-over Barbell Row",
        description = """
            **Setup:** Hinge at the hips until your torso is nearly parallel to the floor. Grip the bar with an overhand grip.

            **Execution:** Pull the bar to your lower stomach, keeping your elbows close to your body. Lower the bar slowly.

            **Pro-Tip:** Keep your neck neutral (look at the floor a few feet in front of you) to avoid neck strain.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Rhomboids, Biceps, Erector Spinae",
    ),
    DefaultExerciseData(
        name = "One-arm Dumbbell Row",
        description = """
            **Setup:** Place one knee and one hand on a bench for support. Hold a dumbbell in the other hand.

            **Execution:** Pull the weight toward your hip, focusing on lifting with your elbow rather than your hand.

            **Pro-Tip:** Imagine you are "sawing wood." This allows you to focus on each side of the back individually.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Rhomboids, Biceps, Brachialis",
    ),
    DefaultExerciseData(
        name = "T-Bar Row",
        description = """
            **Setup:** Use a T-bar machine or landmine attachment. Straddle the bar and grip the handles.

            **Execution:** Pull the weight to your chest while keeping your back flat and knees slightly bent.

            **Pro-Tip:** If using plates, use smaller ones (25lbs) to get a better range of motion at the top.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Traps, Biceps, Erector Spinae",
    ),
    DefaultExerciseData(
        name = "Face Pulls",
        description = """
            **Setup:** Set a cable to forehead height with a rope attachment.

            **Execution:** Pull the rope toward your face, pulling the ends of the rope apart as you get closer to your forehead.

            **Pro-Tip:** This is essential for shoulder health and "rear delt" development. High reps (15-20) work best.
        """.trimIndent(),
        tags = "Back, Rear Deltoids, Traps, Rhomboids, Rotator Cuff",
    ),
    DefaultExerciseData(
        name = "Back Extension",
        description = """
            **Setup:** Place your hips on the pad of a hyper-extension bench. Cross your arms over your chest.

            **Execution:** Lower your torso until it's at a 90° angle. Lift back up until your body is in a straight line.

            **Pro-Tip:** Do not over-extend (arch) your back at the top; stop when your body is straight.
        """.trimIndent(),
        tags = "Back, Erector Spinae, Gluteus Maximus, Hamstrings",
    ),
    DefaultExerciseData(
        name = "Barbell Shrug",
        description = """
            **Setup:** Stand holding a barbell in front of your thighs with an overhand grip.

            **Execution:** Lift your shoulders as high as possible toward your ears. Hold for a second, then lower.

            **Pro-Tip:** Do not roll your shoulders in circles; move them straight up and down to avoid joint irritation.
        """.trimIndent(),
        tags = "Back, Trapezius, Forearms",
    ),
    DefaultExerciseData(
        name = "Dumbbell Shrug",
        description = """
            **Setup:** Stand with dumbbells at your sides, palms facing your body.

            **Execution:** Shrug your shoulders vertically. Keep your arms straight—don't "curl" the weight.

            **Pro-Tip:** Holding the dumbbells at your sides is often more comfortable for the neck than a barbell in front.
        """.trimIndent(),
        tags = "Back, Trapezius, Forearms",
    ),
    DefaultExerciseData(
        name = "Straight Arm Pulldown",
        description = """
            **Setup:** Stand at a cable machine with a straight bar attached to the high pulley.

            **Execution:** Keeping your arms almost locked, pull the bar down to your thighs using only your lats.

            **Pro-Tip:** This is one of the few "isolation" movements for the lats. Great as a finisher.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Triceps, Teres Major",
    ),
    DefaultExerciseData(
        name = "Sumo Deadlift",
        description = """
            **Setup:** Stand with a very wide stance and toes pointed out. Grip the bar inside your knees.

            **Execution:** Drive through your heels to stand, keeping your back vertical.

            **Pro-Tip:** This puts more emphasis on the glutes and inner thighs than a traditional deadlift.
        """.trimIndent(),
        tags = "Back, Gluteus Maximus, Quadriceps, Adductors, Hamstrings, Traps",
    ),
    DefaultExerciseData(
        name = "Single-arm Lat Pulldown",
        description = """
            **Setup:** Use a single handle on the lat pulldown machine.

            **Execution:** Pull the handle down to your side, focusing on driving the elbow into your ribcage.

            **Pro-Tip:** This helps fix imbalances where one side of your back might be stronger than the other.
        """.trimIndent(),
        tags = "Back, Latissimus Dorsi, Biceps",
    ),
    DefaultExerciseData(
        name = "Reverse Fly (Cable)",
        description = """
            **Setup:** Stand in the center of a cable crossover machine with the pulleys set to chest height. Reach across your body to grab the left handle with your right hand and the right handle with your left hand (cables will cross).

            **Execution:** Stand tall with a slight bend in your elbows. Pull your arms out and back in a horizontal arc until they are level with your shoulders, squeezing your shoulder blades together. Slowly return to the crossed position.

            **Pro-Tip:** Avoid "shrugging" your shoulders toward your ears; keep your traps relaxed to ensure the rear deltoids are doing the majority of the work.
        """.trimIndent(),
        tags = "Back, Rear Deltoids, Rhomboids, Traps",
    ),
    // endregion

    // region Shoulders (31-42)
    DefaultExerciseData(
        name = "Overhead Barbell Press",
        description = """
            **Setup:** Hold a bar at shoulder height. Stand with feet shoulder-width apart and core tight.

            **Execution:** Press the bar straight up. As it clears your head, move your head forward "through the window."

            **Pro-Tip:** Squeeze your glutes hard during the lift; this stabilizes your spine and prevents lower back arching.
        """.trimIndent(),
        tags = "Shoulders, Anterior/Lateral Deltoids, Triceps, Traps, Core",
    ),
    DefaultExerciseData(
        name = "Seated Dumbbell Press",
        description = """
            **Setup:** Sit on a bench with back support. Hold dumbbells at ear level, palms facing forward.

            **Execution:** Press the weights up until they meet at the top. Lower them slowly back to ear height.

            **Pro-Tip:** Don't lock your elbows out at the top to keep the tension on the deltoids.
        """.trimIndent(),
        tags = "Shoulders, Anterior/Lateral Deltoids, Triceps",
    ),
    DefaultExerciseData(
        name = "Dumbbell Lateral Raise",
        description = """
            **Setup:** Stand with dumbbells at your sides. Maintain a very slight bend in the elbows.

            **Execution:** Lift the weights out to the sides until they reach shoulder height. Lower them slowly.

            **Pro-Tip:** Lead with your elbows and imagine you are "pouring a pitcher of water" at the top of the movement.
        """.trimIndent(),
        tags = "Shoulders, Lateral Deltoids, Anterior Deltoids, Traps",
    ),
    DefaultExerciseData(
        name = "Dumbbell Front Raise",
        description = """
            **Setup:** Stand with weights in front of your thighs.

            **Execution:** Lift the weights directly in front of you until they are level with your eyes.

            **Pro-Tip:** Alternating arms can help you focus on form and prevent your body from swinging.
        """.trimIndent(),
        tags = "Shoulders, Anterior Deltoids, Lateral Deltoids, Traps",
    ),
    DefaultExerciseData(
        name = "Rear Delt Fly",
        description = """
            **Setup:** Bend over at the hips until your torso is parallel to the floor. Weights hang below your chest.

            **Execution:** Lift the weights out to the sides, squeezing your shoulder blades together at the top.

            **Pro-Tip:** Use lighter weights than you think; the rear delt is a small muscle that is easily overpowered by the back.
        """.trimIndent(),
        tags = "Shoulders, Rear Deltoids, Rhomboids, Traps",
    ),
    DefaultExerciseData(
        name = "Arnold Press",
        description = """
            **Setup:** Hold dumbbells in front of your shoulders with palms facing you (like the top of a bicep curl).

            **Execution:** Press the weights up while rotating your palms outward so they face forward at the top.

            **Pro-Tip:** Named after Schwarzenegger, this rotation hits all three heads of the deltoid.
        """.trimIndent(),
        tags = "Shoulders, Anterior/Lateral Deltoids, Triceps",
    ),
    DefaultExerciseData(
        name = "Upright Row",
        description = """
            **Setup:** Hold a barbell or dumbbells in front of your thighs with a narrow grip.

            **Execution:** Pull the weight vertically toward your chin, flaring your elbows out and up.

            **Pro-Tip:** Stop when your elbows reach shoulder height to avoid impingement in the shoulder joint.
        """.trimIndent(),
        tags = "Shoulders, Lateral Deltoids, Traps, Biceps",
    ),
    DefaultExerciseData(
        name = "Push Press",
        description = """
            **Setup:** Same as the overhead press, but with a slight "power" stance.

            **Execution:** Dip your knees slightly and use the momentum of standing up to help drive the bar overhead.

            **Pro-Tip:** This allows you to move more weight than a strict press, building explosive power.
        """.trimIndent(),
        tags = "Shoulders, Deltoids, Quadriceps, Triceps, Core",
    ),
    DefaultExerciseData(
        name = "Handstand Push-up",
        description = """
            **Setup:** Kick up into a handstand against a wall.

            **Execution:** Lower your head toward the floor by bending your elbows. Press back up until arms are straight.

            **Pro-Tip:** If you can't do a full rep, practice "negatives" by lowering yourself as slowly as possible.
        """.trimIndent(),
        tags = "Shoulders, Deltoids, Triceps, Core, Traps",
    ),
    DefaultExerciseData(
        name = "Landmine Press",
        description = """
            **Setup:** Place one end of a barbell in a corner or landmine attachment. Hold the other end at shoulder height.

            **Execution:** Press the bar upward and forward with one arm.

            **Pro-Tip:** This is very "shoulder-friendly" as the angle is easier on the rotator cuffs than a vertical press.
        """.trimIndent(),
        tags = "Shoulders, Anterior Deltoids, Upper Chest, Triceps",
    ),
    DefaultExerciseData(
        name = "Cable Lateral Raise",
        description = """
            **Setup:** Stand next to a low pulley. Reach across your body to grab the handle.

            **Execution:** Pull the handle out and up to shoulder height.

            **Pro-Tip:** Cables provide "constant tension," making this harder than the dumbbell version because the muscle never rests.
        """.trimIndent(),
        tags = "Shoulders, Lateral Deltoids, Traps",
    ),
    DefaultExerciseData(
        name = "Machine Shoulder Press",
        description = """
            **Setup:** Adjust the seat so the handles are level with your shoulders.

            **Execution:** Press the handles up and return slowly.

            **Pro-Tip:** Great for high-intensity techniques like "rest-pause" sets where you take short breaks to squeeze out more reps.
        """.trimIndent(),
        tags = "Shoulders, Deltoids, Triceps",
    ),
    // endregion

    // region Legs — Quads & Glutes (43-55, 102, 103)
    DefaultExerciseData(
        name = "Barbell Back Squat",
        description = """
            **Setup:** Barbell across your traps. Feet shoulder-width apart, toes slightly pointed out.

            **Execution:** Sit your hips back and down until your thighs are at least parallel to the floor. Drive back up.

            **Pro-Tip:** Keep your chest up and look forward, not down at your feet.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Hamstrings, Core, Erector Spinae",
    ),
    DefaultExerciseData(
        name = "Barbell Front Squat",
        description = """
            **Setup:** Bar across the front of your shoulders, held with fingertips or a cross-arm grip.

            **Execution:** Perform a squat while keeping your elbows pointed high to keep the bar from rolling.

            **Pro-Tip:** This shifts the weight forward, hitting the quadriceps much harder than a back squat.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Core, Upper Back",
    ),
    DefaultExerciseData(
        name = "Goblet Squat",
        description = """
            **Setup:** Hold a single dumbbell or kettlebell against your chest with both hands.

            **Execution:** Squat down, letting your elbows touch the inside of your knees at the bottom.

            **Pro-Tip:** This is the best exercise for beginners to learn proper squatting mechanics and depth.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Core",
    ),
    DefaultExerciseData(
        name = "Leg Press",
        description = """
            **Setup:** Sit in the machine and place feet hip-width apart on the platform.

            **Execution:** Lower the platform toward your chest, then press it back up without locking your knees.

            **Pro-Tip:** Placing your feet higher on the platform hits the glutes; lower feet hit the quads.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Hamstrings",
    ),
    DefaultExerciseData(
        name = "Hack Squat",
        description = """
            **Setup:** Step into the machine with your back against the pad and shoulders under the blocks.

            **Execution:** Lower your body until knees are at 90°. Drive back up.

            **Pro-Tip:** The machine stabilizes your back, allowing you to focus purely on leg drive.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus",
    ),
    DefaultExerciseData(
        name = "Walking Lunges",
        description = """
            **Setup:** Stand tall with dumbbells at your sides or a barbell on your back.

            **Execution:** Take a large step forward and lower your back knee until it almost touches the floor. Step through into the next rep.

            **Pro-Tip:** Keep your torso upright; leaning forward puts more stress on the knees.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Hamstrings, Core",
    ),
    DefaultExerciseData(
        name = "Reverse Lunges",
        description = """
            **Setup:** Stand with feet together.

            **Execution:** Step one foot backward and lower that knee to the floor. Return to the starting position.

            **Pro-Tip:** These are generally easier on the knees than forward lunges.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Hamstrings",
    ),
    DefaultExerciseData(
        name = "Bulgarian Split Squat",
        description = """
            **Setup:** Place one foot on a bench behind you and the other foot forward.

            **Execution:** Squat down on your leading leg. Keep your front shin relatively vertical.

            **Pro-Tip:** Warning: this exercise is notoriously difficult and will result in significant muscle soreness!
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Hamstrings",
    ),
    DefaultExerciseData(
        name = "Leg Extension",
        description = """
            **Setup:** Sit in the machine with the padded bar against your lower shins.

            **Execution:** Straighten your legs completely and squeeze your quads. Lower slowly.

            **Pro-Tip:** This is an "isolation" move; use it to "finish" the quads after heavy squats.
        """.trimIndent(),
        tags = "Legs, Quadriceps",
    ),
    DefaultExerciseData(
        name = "Box Jumps",
        description = """
            **Setup:** Stand in front of a sturdy box or platform.

            **Execution:** Swing your arms, jump, and land with both feet softly on the box in a squat position.

            **Pro-Tip:** Focus on a "soft landing." If you make a loud bang, you're putting too much stress on your joints.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Calves, Core",
    ),
    DefaultExerciseData(
        name = "Step-ups",
        description = """
            **Setup:** Stand in front of a bench. Place one foot firmly on the bench.

            **Execution:** Drive through the top foot to lift your body up. Lower back down slowly.

            **Pro-Tip:** Don't "push off" with the foot on the ground; let the leg on the bench do all the work.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Gluteus Maximus, Hamstrings",
    ),
    DefaultExerciseData(
        name = "Wall Sit",
        description = """
            **Setup:** Lean your back against a wall and slide down until your knees are at a 90° angle.

            **Execution:** Hold this position for as long as possible.

            **Pro-Tip:** Don't rest your hands on your knees; keep them at your sides or across your chest.
        """.trimIndent(),
        tags = "Legs, Quadriceps, Glutes",
    ),
    DefaultExerciseData(
        name = "Sissy Squat",
        description = """
            **Setup:** Hold onto a vertical post for balance.

            **Execution:** Lean your torso backward while bending your knees and coming up on your toes.

            **Pro-Tip:** This provides an incredible stretch and contraction in the lower quads near the knee.
        """.trimIndent(),
        tags = "Legs, Quadriceps",
    ),
    DefaultExerciseData(
        name = "Hip Adductor Machine",
        description = """
            **Setup:** Sit in the machine and adjust the leg pads so they are positioned against your inner knees. Select a comfortable weight and use the lever to open the pads to a wide, comfortable stretch.

            **Execution:** Grip the side handles for stability. Squeeze your thighs together to push the pads toward the center until they nearly touch. Hold the contraction for a moment before slowly allowing the pads to return outward.

            **Pro-Tip:** Keep your back flat against the seat throughout the move; avoiding "cheating" with momentum will better isolate the inner thigh muscles.
        """.trimIndent(),
        tags = "Legs, Adductors, Glutes",
    ),
    DefaultExerciseData(
        name = "Hip Abduction Machine",
        description = """
            **Setup:** Sit in the machine with your back against the pad. Place your feet on the footrests and ensure the outer part of your knees is resting firmly against the pads.

            **Execution:** Using the muscles on the outside of your hips, push your legs outward as far as possible. Pause at the peak of the movement, then resist the weight as you slowly bring your knees back together.

            **Pro-Tip:** To target the gluteus medius more effectively, try leaning your torso slightly forward while keeping your back straight.
        """.trimIndent(),
        tags = "Legs, Gluteus Medius, Tensor Fasciae Latae, Gluteus Maximus",
    ),
    // endregion

    // region Legs — Hamstrings & Calves (56-65)
    DefaultExerciseData(
        name = "Romanian Deadlift (RDL)",
        description = """
            **Setup:** Hold a barbell at your thighs. Knees slightly bent.

            **Execution:** Hinge at the hips, sliding the bar down your legs until you feel a stretch in the hamstrings. Stand back up.

            **Pro-Tip:** Keep the bar in contact with your legs at all times to protect your lower back.
        """.trimIndent(),
        tags = "Legs, Hamstrings, Gluteus Maximus, Erector Spinae, Forearms",
    ),
    DefaultExerciseData(
        name = "Lying Leg Curl",
        description = """
            **Setup:** Lie face down on the machine with the pad against your heels.

            **Execution:** Curl the weight toward your glutes. Squeeze at the top.

            **Pro-Tip:** Keep your hips pressed into the bench; don't let them "pop up" as you curl.
        """.trimIndent(),
        tags = "Legs, Hamstrings, Gastrocnemius",
    ),
    DefaultExerciseData(
        name = "Seated Leg Curl",
        description = """
            **Setup:** Sit in the machine and secure the lap pad tightly.

            **Execution:** Pull the padded bar down and back toward the bottom of the seat.

            **Pro-Tip:** Flex your toes toward your shins to engage the hamstrings more effectively.
        """.trimIndent(),
        tags = "Legs, Hamstrings, Gastrocnemius",
    ),
    DefaultExerciseData(
        name = "Good Mornings",
        description = """
            **Setup:** Barbell on your back (lower than a squat).

            **Execution:** With straight legs (or a very slight bend), hinge forward until your torso is parallel to the floor.

            **Pro-Tip:** Start with very light weight; this move requires extreme control to be safe for the lower back.
        """.trimIndent(),
        tags = "Legs, Hamstrings, Gluteus Maximus, Erector Spinae",
    ),
    DefaultExerciseData(
        name = "Hip Thrust",
        description = """
            **Setup:** Sit on the floor with your upper back against a bench. Place a padded barbell across your hips.

            **Execution:** Drive your hips up until your body is parallel to the floor. Squeeze your glutes hard.

            **Pro-Tip:** Tuck your chin to your chest throughout the movement to prevent lower back arching.
        """.trimIndent(),
        tags = "Legs, Gluteus Maximus, Hamstrings, Core",
    ),
    DefaultExerciseData(
        name = "Glute Bridge",
        description = """
            **Setup:** Lie on your back on the floor with knees bent and feet flat.

            **Execution:** Lift your hips toward the ceiling.

            **Pro-Tip:** This is a "shorter" version of the hip thrust, great for high-rep glute activation.
        """.trimIndent(),
        tags = "Legs, Gluteus Maximus, Hamstrings",
    ),
    DefaultExerciseData(
        name = "Standing Calf Raise",
        description = """
            **Setup:** Stand on the edge of a step or machine with heels hanging off.

            **Execution:** Rise up onto your toes, then lower your heels as far as possible for a stretch.

            **Pro-Tip:** Pause at the bottom and the top of every rep to remove the "bounce" from your Achilles tendon.
        """.trimIndent(),
        tags = "Legs, Gastrocnemius, Soleus",
    ),
    DefaultExerciseData(
        name = "Seated Calf Raise",
        description = """
            **Setup:** Sit in the machine with the pads on your knees.

            **Execution:** Lift your heels up by pushing through the balls of your feet.

            **Pro-Tip:** This targets the soleus muscle, which is only worked when the knee is bent.
        """.trimIndent(),
        tags = "Legs, Soleus",
    ),
    DefaultExerciseData(
        name = "Donkey Calf Raise",
        description = """
            **Setup:** Hinge forward at a 90° angle, resting your arms on a bench.

            **Execution:** Perform a calf raise in this hinged position.

            **Pro-Tip:** This was a favorite of Arnold; it puts the hamstrings and calves in a unique position for growth.
        """.trimIndent(),
        tags = "Legs, Gastrocnemius",
    ),
    DefaultExerciseData(
        name = "Stiff-Legged Deadlift",
        description = """
            **Setup:** Similar to RDL, but with legs kept almost entirely locked.

            **Execution:** Lower the bar all the way to the floor (if flexible) and pull back up.

            **Pro-Tip:** This uses a larger range of motion than the RDL and hits the lower back more significantly.
        """.trimIndent(),
        tags = "Legs, Hamstrings, Erector Spinae, Gluteus Maximus",
    ),
    // endregion

    // region Arms — Biceps & Forearms (66-77)
    DefaultExerciseData(
        name = "Barbell Bicep Curl",
        description = """
            **Setup:** Stand tall with an underhand grip on a barbell.

            **Execution:** Curl the bar toward your shoulders. Keep your elbows glued to your ribcage.

            **Pro-Tip:** Don't swing your body to get the weight up; if you have to swing, the weight is too heavy.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii, Brachialis, Forearms",
    ),
    DefaultExerciseData(
        name = "Dumbbell Bicep Curl",
        description = """
            **Setup:** Stand with dumbbells at your sides.

            **Execution:** Curl the weights up, rotating your palms to face the ceiling as you lift.

            **Pro-Tip:** Rotating the wrist (supination) is a key function of the bicep.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii, Brachialis, Forearms",
    ),
    DefaultExerciseData(
        name = "Hammer Curl",
        description = """
            **Setup:** Hold dumbbells with palms facing each other (neutral grip).

            **Execution:** Curl the weights up without rotating your wrists.

            **Pro-Tip:** This builds the "thickness" of the arm by targeting the brachialis and the forearm.
        """.trimIndent(),
        tags = "Arms, Brachialis, Brachioradialis, Biceps Brachii",
    ),
    DefaultExerciseData(
        name = "Preacher Curl",
        description = """
            **Setup:** Sit at a preacher bench with your arms resting on the slanted pad.

            **Execution:** Curl the weight up toward your chin. Lower it until arms are almost fully extended.

            **Pro-Tip:** This removes all ability to "cheat," making it one of the best bicep builders.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii, Forearms",
    ),
    DefaultExerciseData(
        name = "Concentration Curl",
        description = """
            **Setup:** Sit on a bench. Rest your elbow against the inside of your thigh.

            **Execution:** Curl a dumbbell toward your chest.

            **Pro-Tip:** Focus intensely on the "peak" contraction at the top of the movement.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii",
    ),
    DefaultExerciseData(
        name = "Incline Dumbbell Curl",
        description = """
            **Setup:** Sit on a bench set to a 45° incline. Let your arms hang straight down behind you.

            **Execution:** Curl the weights up while keeping your elbows pointed at the floor.

            **Pro-Tip:** This puts the bicep in a "stretched" position, which is excellent for muscle growth.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii, Forearms",
    ),
    DefaultExerciseData(
        name = "Cable Bicep Curl",
        description = """
            **Setup:** Use a low pulley with a straight or EZ-bar attachment.

            **Execution:** Curl the bar toward your chest.

            **Pro-Tip:** The cable keeps tension on the bicep even at the bottom of the rep, unlike dumbbells.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii, Forearms",
    ),
    DefaultExerciseData(
        name = "Zottman Curl",
        description = """
            **Setup:** Hold dumbbells with an underhand grip.

            **Execution:** Curl up normally; at the top, rotate your palms to face down and lower the weight slowly.

            **Pro-Tip:** This is a "two-in-one" move for both biceps and forearms.
        """.trimIndent(),
        tags = "Arms, Biceps Brachii, Brachioradialis, Forearms",
    ),
    DefaultExerciseData(
        name = "Reverse Barbell Curl",
        description = """
            **Setup:** Hold a bar with an overhand grip (palms facing down).

            **Execution:** Curl the bar up. Your range of motion will be slightly shorter than a regular curl.

            **Pro-Tip:** This specifically targets the brachioradialis (the thick muscle on the top of your forearm).
        """.trimIndent(),
        tags = "Arms, Brachioradialis, Brachialis",
    ),
    DefaultExerciseData(
        name = "Wrist Curls",
        description = """
            **Setup:** Sit on a bench with your forearms resting on your thighs, palms facing up.

            **Execution:** Curl your wrists upward to lift the weight.

            **Pro-Tip:** Use a slow, controlled motion to avoid wrist strain.
        """.trimIndent(),
        tags = "Arms, Forearm Flexors",
    ),
    DefaultExerciseData(
        name = "Reverse Wrist Curls",
        description = """
            **Setup:** Same as wrist curls, but with palms facing down.

            **Execution:** Lift the back of your hands toward the ceiling.

            **Pro-Tip:** Essential for "completing" the look of the forearm.
        """.trimIndent(),
        tags = "Arms, Forearm Extensors",
    ),
    DefaultExerciseData(
        name = "Spider Curl",
        description = """
            **Setup:** Lie chest-down on an incline bench. Let your arms hang vertically.

            **Execution:** Curl the bar up toward your forehead.

            **Pro-Tip:** Because your arms are in front of your body, this hits the "short head" of the bicep (the inner part).
        """.trimIndent(),
        tags = "Arms, Biceps Brachii",
    ),
    // endregion

    // region Arms — Triceps (78-85)
    DefaultExerciseData(
        name = "Tricep Rope Pushdown",
        description = """
            **Setup:** High pulley with a rope attachment. Grip the rope with palms facing each other.

            **Execution:** Push down until arms are straight. Pull the ends of the rope apart at the bottom.

            **Pro-Tip:** Keep your shoulders down and back; don't let them hunch forward as you push.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii",
    ),
    DefaultExerciseData(
        name = "Overhead Dumbbell Extension",
        description = """
            **Setup:** Sit or stand. Hold one dumbbell with both hands directly over your head.

            **Execution:** Lower the weight behind your neck by bending your elbows. Press it back up.

            **Pro-Tip:** Keep your elbows tucked in close to your ears; don't let them flare out.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii",
    ),
    DefaultExerciseData(
        name = "Skull Crushers",
        description = """
            **Setup:** Lie on a bench. Hold an EZ-bar above your chest.

            **Execution:** Lower the bar to your forehead (or slightly behind your head) by bending only the elbows. Press back up.

            **Pro-Tip:** Lowering the bar *behind* your head instead of to your forehead increases the stretch on the long head of the tricep.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii, Forearms",
    ),
    DefaultExerciseData(
        name = "Bench Dips",
        description = """
            **Setup:** Place your hands on the edge of a bench. Place your feet on the floor or another bench.

            **Execution:** Lower your hips toward the floor. Press back up until arms are straight.

            **Pro-Tip:** To make it harder, place a weight plate on your lap.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii, Anterior Deltoids, Pectoralis Major",
    ),
    DefaultExerciseData(
        name = "Close-grip Bench Press",
        description = """
            **Setup:** Lie on a bench. Grip the bar with hands about 6-8 inches apart.

            **Execution:** Lower the bar to your lower chest, keeping your elbows tucked against your ribs.

            **Pro-Tip:** This is the best compound movement for adding massive strength to the triceps.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii, Pectoralis Major, Anterior Deltoids",
    ),
    DefaultExerciseData(
        name = "Tricep Kickbacks",
        description = """
            **Setup:** Hinge forward. Hold a dumbbell with your upper arm parallel to the floor.

            **Execution:** Extend your forearm backward until your arm is perfectly straight.

            **Pro-Tip:** Don't swing the weight. If your upper arm moves, you're using momentum, not muscle.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii",
    ),
    DefaultExerciseData(
        name = "Parallel Bar Dips",
        description = """
            **Setup:** Grip parallel bars. Keep your body as vertical as possible.

            **Execution:** Lower yourself until elbows are at 90°. Push back up.

            **Pro-Tip:** Keeping the torso upright shifts the load from the chest to the triceps.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii, Pectoralis Major, Deltoids",
    ),
    DefaultExerciseData(
        name = "Dumbbell Floor Press (Close Grip)",
        description = """
            **Setup:** Lie on the floor. Hold two dumbbells so they are touching each other above your chest.

            **Execution:** Lower the weights until your elbows touch the floor. Press back up.

            **Pro-Tip:** The "touching" of the dumbbells creates a massive contraction in the triceps and inner chest.
        """.trimIndent(),
        tags = "Arms, Triceps Brachii, Inner Chest",
    ),
    // endregion

    // region Core (86-97, 105)
    DefaultExerciseData(
        name = "Plank",
        description = """
            **Setup:** Get into a push-up position but rest on your forearms.

            **Execution:** Hold a perfectly straight line from head to heels. Squeeze your core and glutes.

            **Pro-Tip:** It's better to do a perfect 30-second plank than a sloppy 2-minute plank.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis, Transverse Abdominis, Glutes, Shoulders",
    ),
    DefaultExerciseData(
        name = "Side Plank",
        description = """
            **Setup:** Lie on your side. Support your weight on one forearm and the side of your foot.

            **Execution:** Lift your hips so your body forms a straight line. Hold.

            **Pro-Tip:** Don't let your hips "dip" toward the floor; keep them pushed up high.
        """.trimIndent(),
        tags = "Core, Obliques, Shoulders",
    ),
    DefaultExerciseData(
        name = "Crunches",
        description = """
            **Setup:** Lie on your back with knees bent and feet flat.

            **Execution:** Lift only your head and shoulder blades off the floor. Squeeze your abs.

            **Pro-Tip:** Don't pull on your neck with your hands; keep hands behind ears or across your chest.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis",
    ),
    DefaultExerciseData(
        name = "Reverse Crunches",
        description = """
            **Setup:** Lie on your back. Lift your legs so your knees are at 90°.

            **Execution:** Use your lower abs to lift your hips off the floor and pull your knees toward your chest.

            **Pro-Tip:** Lower your hips slowly to maximize the "eccentric" burn in the lower abs.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis",
    ),
    DefaultExerciseData(
        name = "Hanging Leg Raises",
        description = """
            **Setup:** Hang from a pull-up bar.

            **Execution:** Keeping your legs straight, lift them until they are parallel to the floor.

            **Pro-Tip:** To make it easier, perform "hanging knee raises" by bending your knees.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis, Iliopsoas",
    ),
    DefaultExerciseData(
        name = "Russian Twists",
        description = """
            **Setup:** Sit with knees bent and feet slightly off the floor. Hold a weight or medicine ball.

            **Execution:** Rotate your torso to touch the weight to the floor on your left, then your right.

            **Pro-Tip:** Follow the weight with your eyes; this ensures your entire torso is rotating.
        """.trimIndent(),
        tags = "Core, Obliques, Rectus Abdominis",
    ),
    DefaultExerciseData(
        name = "Mountain Climbers",
        description = """
            **Setup:** Start in a high plank position.

            **Execution:** Rapidly drive one knee toward your chest, then switch.

            **Pro-Tip:** Keep your hips low; don't let your "butt" jump up into the air as you run.
        """.trimIndent(),
        tags = "Core, Shoulders, Hip Flexors",
    ),
    DefaultExerciseData(
        name = "Ab Wheel Rollout",
        description = """
            **Setup:** Kneel on the floor holding the handles of an ab wheel.

            **Execution:** Roll the wheel forward as far as you can without your back arching. Pull back using your abs.

            **Pro-Tip:** This is an advanced move. Only go as far as you can while maintaining a "hollow" body position.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis, Lats, Shoulders",
    ),
    DefaultExerciseData(
        name = "Bicycle Crunches",
        description = """
            **Setup:** Lie on your back. Bring your knees to 90° and hands behind your head.

            **Execution:** Bring your right elbow to your left knee while straightening your right leg. Repeat on the other side.

            **Pro-Tip:** Focus on the rotation of the torso rather than just moving your elbows.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis, Obliques",
    ),
    DefaultExerciseData(
        name = "Woodchoppers",
        description = """
            **Setup:** Use a cable machine set to high. Stand sideways to the machine.

            **Execution:** Pull the handle diagonally down across your body to your opposite knee.

            **Pro-Tip:** Pivot your back foot as you rotate to protect your knees.
        """.trimIndent(),
        tags = "Core, Obliques, Shoulders",
    ),
    DefaultExerciseData(
        name = "Dead Bug",
        description = """
            **Setup:** Lie on your back. Arms pointing up, knees at 90°.

            **Execution:** Slowly lower your right arm and left leg toward the floor simultaneously. Return and switch.

            **Pro-Tip:** The goal is to keep your lower back pressed firmly against the floor the entire time.
        """.trimIndent(),
        tags = "Core, Transverse Abdominis, Rectus Abdominis",
    ),
    DefaultExerciseData(
        name = "V-ups",
        description = """
            **Setup:** Lie flat on your back with arms extended above your head.

            **Execution:** Simultaneously lift your torso and legs to touch your toes at the top.

            **Pro-Tip:** This requires significant core strength and timing; keep the movement controlled.
        """.trimIndent(),
        tags = "Core, Rectus Abdominis",
    ),
    DefaultExerciseData(
        name = "Bird-Dog",
        description = """
            **Setup:** Start on your hands and knees in a tabletop position. Ensure your hands are directly under your shoulders and your knees are directly under your hips with a flat, neutral spine.

            **Execution:** Simultaneously extend your right arm straight ahead and your left leg straight back until both are parallel to the floor. Hold for 2–3 seconds while balancing, then slowly lower them and repeat with the opposite arm and leg.

            **Pro-Tip:** Imagine there is a glass of water resting on your lower back; your goal is to move so smoothly and keep your hips so level that not a single drop spills.
        """.trimIndent(),
        tags = "Core, Erector Spinae, Gluteus Maximus, Shoulders",
    ),
    // endregion

    // region Full Body & Functional (98-100)
    DefaultExerciseData(
        name = "Burpees",
        description = """
            **Setup:** Stand tall.

            **Execution:** Drop into a squat, kick feet back to a plank, do a push-up, jump feet back in, and jump into the air.

            **Pro-Tip:** If the push-up is too hard, you can skip it and just jump your feet back.
        """.trimIndent(),
        tags = "Full Body, Quadriceps, Pectoralis Major, Shoulders, Triceps, Core",
    ),
    DefaultExerciseData(
        name = "Kettlebell Swings",
        description = """
            **Setup:** Stand with feet wide. Hold a kettlebell with both hands between your legs.

            **Execution:** Hinge at the hips (don't squat!) and use a powerful hip snap to swing the bell to chest height.

            **Pro-Tip:** The power comes from your glutes and hamstrings, not your arms. Your arms are just "ropes."
        """.trimIndent(),
        tags = "Full Body, Hamstrings, Gluteus Maximus, Erector Spinae, Shoulders, Forearms",
    ),
    DefaultExerciseData(
        name = "Farmer's Walk",
        description = """
            **Setup:** Hold the heaviest dumbbells or kettlebells you can manage at your sides.

            **Execution:** Walk for a set distance or time while keeping your chest high and shoulders back.

            **Pro-Tip:** This is the ultimate "functional" move for grip strength, core stability, and posture.
        """.trimIndent(),
        tags = "Full Body, Forearms, Trapezius, Core, Legs, Upper Back",
    ),
    // endregion
)
