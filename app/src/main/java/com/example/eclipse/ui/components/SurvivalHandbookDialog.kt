package com.example.eclipse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseCrimsonGlow
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary
import com.example.ui.theme.EclipseWarningAmber

@Composable
fun SurvivalHandbookDialog(
    language: Language,
    onDismiss: () -> Unit
) {
    val isAr = language == Language.ARABIC
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Survival, 1: Monsters, 2: Lockers, 3: Controls

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("survival_handbook_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C0E14),
            border = androidx.compose.foundation.BorderStroke(2.dp, EclipseCrimson)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background subtle parchment art
                Image(
                    painter = painterResource(id = R.drawable.img_survival_journal),
                    contentDescription = "Survival Handbook Paper",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.22f
                )

                // Foreground Paper Dossier
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Header Bar (Field Document)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Handbook",
                                tint = EclipseCrimsonGlow,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "مذكرة النجاة الميدانية (بين يديك)" else "FIELD SURVIVAL DOSSIER (HELD IN HAND)",
                                color = EclipseTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_handbook_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = EclipseTextSecondary
                            )
                        }
                    }

                    // Interactive Document Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HandbookTabChip(
                            label = if (isAr) "النجاة والمصباح" else "Survival & Light",
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        HandbookTabChip(
                            label = if (isAr) "سجل الوحوش" else "Monsters",
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        HandbookTabChip(
                            label = if (isAr) "الخزائن والمخابئ" else "Lockers & Stealth",
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                        HandbookTabChip(
                            label = if (isAr) "التحكم والأزرار" else "Controls & HUD",
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 }
                        )
                    }

                    // Aged Weathered Paper Reading Body
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = Color(0xEE161922),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66444D5E))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                when (selectedTab) {
                                    0 -> {
                                        // TAB 0: SURVIVAL & FLASHLIGHT
                                        Text(
                                            text = if (isAr) "قواعد البقاء وإدارة بطارية المصباح:" else "SURVIVAL PROTOCOLS & BATTERY MANAGEMENT:",
                                            color = EclipseWarningAmber,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = if (isAr)
                                                "• المصباح اليدوي التكتيكي: يوفر شعاع ضوء واقعي مخروطي لكشف البيئة المظلمة. عند انخفاض شحن البطارية دون 20%، يبدأ المصباح بالوميض الكهربائي المفاجئ.\n\n" +
                                                "• الحفاظ على الشحن: احرص على إطفاء المصباح عند الوقوف أو التسلل. نفاذ الشحن يجعلك غارقاً في ظلام دامس مرعب.\n\n" +
                                                "• خلايا الليثيوم الخضراء: متناثرة في الغرف وعلى الرفوف؛ التقاطها يعيد شحن البطارية إلى 100% فوراً.\n\n" +
                                                "• مؤشر الرعب ودقات القلب: عند اقتراب الكيانات، ترتفع وتيرة نبضات قلبك ويبدأ الهاتف بالاهتزاز اللمسي."
                                            else
                                                "• TACTICAL FLASHLIGHT: Projects a realistic cone beam piercing the pitch darkness. Below 20% battery, voltage fluctuations cause erratic flickering.\n\n" +
                                                "• BATTERY CONSERVATION: Turn off the flashlight when creeping through safe rooms. Complete depletion plunges you into absolute pitch darkness.\n\n" +
                                                "• GREEN LITHIUM CELLS: Scattered in lockers, on desks, and in medical carts. Picking one up reloads the torch to 100%.\n\n" +
                                                "• HEARTBEAT PANIC SYSTEM: As supernatural entities approach, your heartbeat accelerates and haptics pulse violently.",
                                            color = EclipseTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 21.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    1 -> {
                                        // TAB 1: MONSTERS & BEHAVIOR
                                        Text(
                                            text = if (isAr) "ملف الوحوش والذكاء الاصطناعي:" else "MONSTER ENCYCLOPEDIA & AI BEHAVIORS:",
                                            color = EclipseCrimson,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = if (isAr)
                                                "1. المراقب (THE OBSERVER):\n" +
                                                "كيان طويل غامض بعيون بيضاء مشعة. يراقب تحركاتك في صمت، يتحقق من مصادر الضوء، ويتحول لونه للأحمر الدموي ويبدأ المطاردة إذا لمحك!\n\n" +
                                                "2. صياد الأصوات (SOUND HUNTER):\n" +
                                                "أعمى تماماً ولا يرى الضوء، لكنه يمتلك سمعاً خارقاً. الركض بالقرب منه يجعله يندفع نحوك بسرعة البرق. استخدم زر التسلل (Crouch) لتمر بجانبه بأمان.\n\n" +
                                                "3. الكائن الكامن (THE LURKER):\n" +
                                                "يبقى ساكناً في زوايا الغرف المظلمة؛ الاقتراب المباشر منه يستفزه للهجوم.\n\n" +
                                                "4. حارس المنشأة (FACILITY SENTINEL):\n" +
                                                "يحرس بوابات المعامل الحساسة برؤوس مستشعرات ليزرية حمراء."
                                            else
                                                "1. THE OBSERVER:\n" +
                                                "Tall, shadowy entity with piercing glowing eyes. Patrols silently, investigates light, and turns violent crimson when locked onto you!\n\n" +
                                                "2. SOUND HUNTER:\n" +
                                                "Completely blind, but boasts superhuman auditory senses. Sprinting triggers a lethal rush. Always Crouch to bypass silently.\n\n" +
                                                "3. THE LURKER:\n" +
                                                "Stationary shadow lurking in corners; attacking if breached within close proximity.\n\n" +
                                                "4. FACILITY SENTINEL:\n" +
                                                "Automated biocontainment warden guarding high-security laboratory gates.",
                                            color = EclipseTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 21.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    2 -> {
                                        // TAB 2: LOCKERS & HIDING
                                        Text(
                                            text = if (isAr) "الخزائن والمخابئ الواقعية (Hiding Spots):" else "REALISTIC LOCKERS & STEALTH HIDING:",
                                            color = EclipseEerieCyan,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = if (isAr)
                                                "• دخول الخزانة: اقترب من الخزانة المعدنية واضغط على زر التفاعل. يدخل اللاعب ويغلق الباب بحذر.\n\n" +
                                                "• الرؤية من فتحات التهوية (Slits): أثناء وجودك داخل الخزانة، ترى الممر الخارجي عبر فتحات الستائر المعدنية ثلاثية الأبعاد.\n\n" +
                                                "• كتم الأنفاس: صوتك يصبح مكتملاً ومكتوماً تماماً، ويتنفس اللاعب برفق بينما يمر 'المراقب' بجانب الخزانة مباشرة دون أن يراك.\n\n" +
                                                "• الخروج الآمن: اضغط على زر 'الخروج من الخزانة' عندما تبتعد الخطوات وتطمئن لخلو الممر."
                                            else
                                                "• ENTERING A LOCKER: Approach metal lockers and tap the interaction prompt. The player enters and quietly latches the door.\n\n" +
                                                "• LOUVER PEEPHOLES: Inside the locker, you can peek through the 3D metal ventilation slits at the corridor outside.\n\n" +
                                                "• MUFFLED BREATHING: Your noise level drops to absolute zero. The Observer can pace right outside without detecting you.\n\n" +
                                                "• SAFE EXIT: Press 'Exit Locker' once the heavy footsteps fade into the distance.",
                                            color = EclipseTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 21.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    3 -> {
                                        // TAB 3: CONTROLS & HUD CUSTOMIZATION
                                        Text(
                                            text = if (isAr) "تخصيص أزرار التحكم وشاشات اللمس:" else "TOUCH CONTROLS & BUTTON CUSTOMIZATION:",
                                            color = EclipseTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = if (isAr)
                                                "• عصا الحركة (يسار): تحريك للأمام والخلف والمناورة الجانبية بسلاسة.\n" +
                                                "• سحب الرؤية (يمين): اسحب في أي مكان على النصف الأيمن للنظر والالتفاف بحرية تامة.\n" +
                                                "• زر المصباح (Flashlight): تشغيل وإطفاء الكشاف مع عداد بطارية حي.\n" +
                                                "• زر التسلل (Crouch): كتم خطواتك (0 dB) لتجاوز الوحوش الحساسة للصوت.\n" +
                                                "• زر الركض (Sprint): ركض أسرع على حساب التحمل والصوت العالي.\n" +
                                                "• زر الحقيبة (Inventory): استخدام حقن الإسعاف، فحص المفاتيح والوثائق.\n\n" +
                                                "• تخصيص الأزرار في الإعدادات:\n" +
                                                "يمكنك في أي وقت تكبير الأزرار حتى 150%، تغيير موضعها (يمين، مرتفع، معكوس للأعسر، موزع على الجانبين)، وضبط شفافيتها وارتفاعها!"
                                            else
                                                "• MOVEMENT JOYSTICK (Left): Smooth directional walking and strafing.\n" +
                                                "• LOOK TOUCHPAD (Right): Swipe anywhere on the right half to pan and look.\n" +
                                                "• FLASHLIGHT BUTTON: Snaps mechanical power with real-time battery readout.\n" +
                                                "• SNEAK / CROUCH: Drops footstep noise to 0 dB.\n" +
                                                "• SPRINT: High-speed escape burning stamina.\n" +
                                                "• INVENTORY BAG: Apply medical syringes, inspect keys and audio logs.\n\n" +
                                                "• BUTTON CUSTOMIZATION IN SETTINGS:\n" +
                                                "Resize controls up to 150%, select presets (Default, Raised, Lefty, Split), adjust vertical margins and transparency anytime!",
                                            color = EclipseTextSecondary,
                                            fontSize = 13.sp,
                                            lineHeight = 21.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Realistic Survivor Thumbs Gripping Paper at Bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left thumb grip
                        Surface(
                            color = Color(0xFF4E3E36),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                            modifier = Modifier
                                .width(38.dp)
                                .height(16.dp)
                        ) {}

                        // Center classified stamp
                        Text(
                            text = if (isAr) "[ وثيقة سرية للمشروع ]" else "[ CLASSIFIED PROJECT DOSSIER ]",
                            color = EclipseCrimsonGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Right thumb grip
                        Surface(
                            color = Color(0xFF4E3E36),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                            modifier = Modifier
                                .width(38.dp)
                                .height(16.dp)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun HandbookTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) EclipseCrimson else Color(0x66222834),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) EclipseCrimsonGlow else Color(0x33556070)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xCCFFFFFF),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
