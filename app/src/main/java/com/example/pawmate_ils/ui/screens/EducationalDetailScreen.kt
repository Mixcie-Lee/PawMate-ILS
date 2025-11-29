package com.example.pawmate_ils.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.ThemeManager

data class EducationalStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)

data class EducationalContent(
    val title: String,
    val videoId: String,
    val steps: List<EducationalStep>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationalDetailScreen(
    navController: NavController,
    articleId: Int
) {
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val context = LocalContext.current

    val content = getEducationalContent(articleId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = content.title,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${content.videoId}"))
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://img.youtube.com/vi/${content.videoId}/maxresdefault.jpg",
                            contentDescription = "Video thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color.Red.copy(alpha = 0.9f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                )
                            }
                            Text(
                                text = "Tap to watch on YouTube",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            items(content.steps.size) { index ->
                val step = content.steps[index]
                StepCard(
                    step = step,
                    textColor = textColor
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StepCard(
    step: EducationalStep,
    textColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Step ${step.stepNumber}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = step.description,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )
    }
}

fun getEducationalContent(articleId: Int): EducationalContent {
    return when (articleId) {
        1 -> EducationalContent(
            title = "Complete Guide to Dog Nutrition",
            videoId = "Zb3Wzs2FcFE",
            steps = listOf(
                EducationalStep(1, "Understand Essential Nutrients", "Dogs need proteins for muscle development, fats for energy and coat health, carbohydrates for sustained energy, and vitamins and minerals for overall body function. Quality protein sources include chicken, beef, fish, and eggs."),
                EducationalStep(2, "Read Dog Food Labels Correctly", "Look for AAFCO certification, check that meat is the first ingredient, avoid foods with excessive fillers like corn or wheat, and ensure the formula matches your dog's life stage (puppy, adult, or senior)."),
                EducationalStep(3, "Calculate Proper Portions", "Use your dog's weight and activity level to determine daily calories needed. Generally, active dogs need 30 calories per pound, while less active dogs need 20 calories per pound. Divide into 2-3 meals daily."),
                EducationalStep(4, "Know Toxic Foods to Avoid", "Never feed dogs chocolate, grapes, raisins, onions, garlic, xylitol (artificial sweetener), avocados, macadamia nuts, or alcohol. These can cause serious health issues or be fatal."),
                EducationalStep(5, "Maintain Proper Hydration", "Provide fresh water at all times. Dogs need approximately 1 ounce of water per pound of body weight daily. Monitor for signs of dehydration like dry gums, lethargy, or excessive panting."),
                EducationalStep(6, "Adjust for Life Stages", "Puppies need high-protein, calorie-dense food for growth. Adult dogs require balanced maintenance diets. Senior dogs benefit from lower-calorie foods with joint support supplements like glucosamine."),
                EducationalStep(7, "Choose Healthy Treats Wisely", "Treats should make up no more than 10% of daily calories. Opt for natural options like carrots, green beans, or plain cooked chicken. Consider supplements like fish oil for coat health after consulting your vet.")
            )
        )
        2 -> EducationalContent(
            title = "Understanding Cat Behavior",
            videoId = "zPOAaDUzVDY",
            steps = listOf(
                EducationalStep(1, "Decode Tail Positions", "A high, upright tail means happy and confident. Puffed tail indicates fear or aggression. Tucked tail shows submission or fear. Slow swishing means they're focused, while fast thrashing signals irritation."),
                EducationalStep(2, "Interpret Vocalizations", "Short meows are greetings, long meows request attention or food. Purring usually means contentment but can also indicate pain. Hissing and growling are warnings. Chirping or chattering at birds shows hunting instincts."),
                EducationalStep(3, "Recognize Hunting Play", "Cats need 15-30 minutes of interactive play daily. Use toys that mimic prey like feather wands or laser pointers. Let them 'catch' the toy to satisfy their instinct. Play before meals mimics natural hunt-eat pattern."),
                EducationalStep(4, "Understand Personal Space Needs", "Cats show affection through slow blinks, head bumps, and kneading. Respect their boundaries - a twitching tail or flattened ears means stop petting. Some cats prefer to sit near you rather than on you."),
                EducationalStep(5, "Provide Proper Scratching Outlets", "Scratching marks territory and maintains claw health. Offer multiple scratching posts (vertical and horizontal), place near sleeping areas. Use catnip or treats to encourage use. Never declaw - it's painful and causes behavioral issues."),
                EducationalStep(6, "Identify Stress Signs", "Watch for hiding, loss of appetite, excessive grooming, or litter box avoidance. Provide vertical spaces, hiding spots, and routine. Use pheromone diffusers for anxiety. Sudden behavior changes warrant a vet visit."),
                EducationalStep(7, "Address Behavioral Issues Early", "Aggression, inappropriate elimination, or destructive behavior often stems from medical issues, stress, or boredom. Consult your vet first to rule out health problems, then work with a feline behaviorist if needed.")
            )
        )
        3 -> EducationalContent(
            title = "First Aid for Pets",
            videoId = "XtOktV6RrrU",
            steps = listOf(
                EducationalStep(1, "Assemble Emergency Supplies", "Keep a pet first aid kit with gauze, bandages, hydrogen peroxide, tweezers, digital thermometer, muzzle, blanket, and emergency vet contacts. Store it in an accessible location and keep one in your car."),
                EducationalStep(2, "Recognize True Emergencies", "Seek immediate vet care for difficulty breathing, unconsciousness, severe bleeding, seizures, bloated abdomen, eye injuries, suspected poisoning, inability to urinate, or body temperature above 104°F or below 99°F."),
                EducationalStep(3, "Treat Minor Wounds Properly", "For small cuts, rinse with clean water, apply pressure to stop bleeding, clean with diluted antiseptic, and cover with gauze. See a vet if bleeding doesn't stop in 5 minutes or wound is deep."),
                EducationalStep(4, "Perform Pet CPR Correctly", "Check for breathing and pulse. For dogs: 30 chest compressions then 2 breaths. For cats: 30 compressions then 1 breath. Compress 1/3 to 1/2 chest depth. Continue until vet arrival or breathing resumes."),
                EducationalStep(5, "Clear Airway Obstructions", "If pet is choking, look in mouth and remove visible objects. For dogs: lift rear legs, apply abdominal thrusts behind ribs. For cats: hold upside down and deliver sharp blows between shoulder blades."),
                EducationalStep(6, "Handle Fractures and Burns", "Don't move pets with suspected fractures - transport on flat board. For burns, flush with cool water for 20 minutes, apply cool compress, never use ice. Keep pet warm and calm during transport."),
                EducationalStep(7, "Respond to Poisoning Fast", "Call ASPCA Poison Control (888-426-4435) immediately. Have product container ready. Induce vomiting ONLY if instructed. Common toxins: chocolate, xylitol, antifreeze, rat poison, and many human medications.")
            )
        )
        4 -> EducationalContent(
            title = "Puppy Training Basics",
            videoId = "JRVCbd4pQOI",
            steps = listOf(
                EducationalStep(1, "Begin Training at 8 Weeks", "Start the day puppy arrives home. Use positive reinforcement with treats and praise. Keep sessions 5-10 minutes, 3-4 times daily. Everyone in household must use same commands and rules consistently."),
                EducationalStep(2, "Master Potty Training", "Take puppy out every 1-2 hours, after meals, play, and naps. Use a designated spot, say 'go potty,' and reward immediately. For accidents, clean thoroughly without scolding. Most puppies are trained by 4-6 months."),
                EducationalStep(3, "Teach Essential Commands", "SIT: Hold treat above nose, move back until they sit, reward. STAY: Start with 3 seconds, gradually increase. COME: Use in safe area, reward heavily when they come. Practice daily in short bursts."),
                EducationalStep(4, "Create a Safe Crate Space", "Choose crate big enough to stand and turn around. Add soft bedding and toys. Feed meals inside. Start with 10-minute intervals, gradually increase. Never use as punishment. Helps with potty training and provides security."),
                EducationalStep(5, "Socialize Before 16 Weeks", "Expose to 100+ people, various dogs (vaccinated), different surfaces, sounds, and environments. Attend puppy classes. Critical period ends at 16 weeks - missed socialization can cause lifelong fear and aggression."),
                EducationalStep(6, "Stop Biting and Redirect Chewing", "When puppy bites, say 'ouch' loudly and stop play. Redirect to appropriate toys. Provide frozen teething toys for sore gums. Puppy-proof home by removing shoes, wires, and valuables. Biting usually stops by 6 months."),
                EducationalStep(7, "Practice Loose Leash Walking", "Start indoors, reward when at your side. Stop walking when they pull, resume when leash loosens. Use high-value treats. Keep walks short initially. Change direction frequently to keep attention on you.")
            )
        )
        5 -> EducationalContent(
            title = "Creating a Balanced Pet Diet",
            videoId = "uDP8nPELClg",
            steps = listOf(
                EducationalStep(1, "Balance Protein, Fat, and Carbs", "Dogs need 18-25% protein, 10-15% fat. Cats need 26-30% protein, 9-15% fat (obligate carnivores). Quality protein sources: chicken, beef, fish. Avoid corn and wheat fillers. Carbs provide energy but shouldn't exceed 50%."),
                EducationalStep(2, "Include Essential Vitamins", "Vitamin A for vision and immunity, D for bone health, E for antioxidants, K for blood clotting. Minerals: calcium and phosphorus for bones (1.2:1 ratio), zinc for skin, iron for blood. Quality commercial foods include these."),
                EducationalStep(3, "Feed for Life Stage", "Puppies/kittens need 2x adult calories and higher protein. Feed 3-4 times daily until 6 months. Adults need maintenance diets, feed twice daily. Seniors need 20-30% fewer calories, more fiber, joint supplements."),
                EducationalStep(4, "Decode Food Labels", "Ingredients listed by weight - meat should be first. 'Meal' is concentrated protein (good). Avoid 'by-products' and artificial colors. Look for AAFCO statement confirming nutritional adequacy. Check expiration dates."),
                EducationalStep(5, "Consider Homemade Carefully", "Pros: Control ingredients, no preservatives. Cons: Hard to balance nutrients, time-consuming, expensive. If making homemade, consult veterinary nutritionist to ensure complete diet. Most pets do well on quality commercial food."),
                EducationalStep(6, "Identify Food Allergies", "Common signs: itchy skin, ear infections, vomiting, diarrhea. Most common allergens: beef, dairy, wheat, chicken, eggs. Try elimination diet (novel protein + carb) for 8-12 weeks. Work with vet for diagnosis."),
                EducationalStep(7, "Maintain Ideal Body Weight", "Feel ribs easily but don't see them. Visible waist when viewed from above. Tuck-up when viewed from side. Weigh monthly. Adjust portions by 10% if gaining/losing. Overweight pets have shorter lifespans and health problems.")
            )
        )
        6 -> EducationalContent(
            title = "Common Pet Health Issues",
            videoId = "Zb3Wzs2FcFE",
            steps = listOf(
                EducationalStep(1, "Prevent and Treat Parasites", "Use monthly preventatives for fleas, ticks, and heartworms year-round. Deworm puppies/kittens at 2, 4, 6, 8 weeks. Signs: scratching, visible fleas/ticks, worms in stool, coughing. Heartworms are deadly - prevention is essential."),
                EducationalStep(2, "Maintain Dental Health", "80% of pets have dental disease by age 3. Signs: bad breath, yellow teeth, bleeding gums, difficulty eating. Brush teeth 3x weekly with pet toothpaste. Provide dental chews. Get professional cleanings annually under anesthesia."),
                EducationalStep(3, "Address Skin Problems", "Hot spots: red, moist, painful areas from allergies or fleas. Clean and dry area, prevent licking. Allergies cause itching, hair loss, ear infections. Common triggers: food, fleas, pollen. May need allergy testing and medication."),
                EducationalStep(4, "Handle Digestive Issues", "Mild vomiting/diarrhea: withhold food 12 hours, offer water, slowly reintroduce bland diet (boiled chicken + rice). See vet if: blood present, lasts over 24 hours, lethargy, repeated vomiting, or puppy/kitten affected."),
                EducationalStep(5, "Treat Ear Infections", "Signs: head shaking, scratching ears, odor, discharge, redness. Common in floppy-eared breeds. Clean weekly with vet-approved solution. Never use cotton swabs. Chronic infections may indicate allergies. Requires vet diagnosis and medication."),
                EducationalStep(6, "Combat Pet Obesity", "55% of pets are overweight. Risks: diabetes, arthritis, shorter lifespan. Reduce food by 25%, increase exercise gradually. No table scraps. Use low-calorie treats. Aim for 1-2 lbs loss per month. Consult vet for diet plan."),
                EducationalStep(7, "Schedule Preventive Care", "Puppies/kittens: vaccines at 6, 9, 12, 16 weeks. Adults: annual exam, vaccines, heartworm test, fecal check. Seniors (7+): twice yearly exams, bloodwork, urinalysis. Early detection of disease dramatically improves outcomes and reduces costs.")
            )
        )
        else -> EducationalContent(
            title = "Pet Care Guide",
            videoId = "Zb3Wzs2FcFE",
            steps = listOf(
                EducationalStep(1, "Basic Care", "Learn the fundamentals of pet care and wellness.")
            )
        )
    }
}
