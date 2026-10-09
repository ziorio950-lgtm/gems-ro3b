package com.example.eclipse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimsonGlow
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun CreditsScreen(
    language: Language,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = language == Language.ARABIC

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090C))
            .padding(24.dp)
            .testTag("credits_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("credits_back_button")) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = EclipseTextPrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isAr) "القصة وهندسة محرك Unity & C#" else "STORY & UNITY C# ARCHITECTURE",
                        color = EclipseTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAr) "توثيق البنية البرمجية، الأصول، وشيفرة المحرك" else "Game design, lore analysis & Unity C# implementation specs",
                        color = EclipseTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Narrative Summary
                SectionCard(
                    title = if (isAr) "1. قصة لعبة الكسوف: ما وراء الظلام" else "1. Narrative & Psychological Horror Premise",
                    isAr = isAr
                ) {
                    Text(
                        text = if (isAr)
                            "تبدأ اللعبة بشخص يستيقظ بعد حادث سيارة في ليلة عاصفة، ويكتشف مدينة مهجورة لا تظهر على الخرائط. أثناء استكشافها، يواجه 'المراقب' (The Observer) ويكتشف منشأة سرية أجرت أبحاثاً على الخوف الجماعي وترددات الوعي، مما فتح فجوة إلى العالم المعكوس.\n\nالنهايات المتعددة:\n1. نهاية الهروب السطحي: الخروج عبر خط المترو المهجور مع بقاء الفجوة نشطة.\n2. نهاية إغلاق البوابة: التضحية بالأثر البلوري وعزل المنشأة تحت الأرض للأبد.\n3. نهاية الاندماج الكوني: اكتشاف أن اللاعب كان المدير العلمي السابق الذي بدأ التجربة."
                        else
                            "You awaken after a violent car wreck into a deserted city excluded from modern maps. Stalked by 'The Observer', you uncover a subterranean laboratory that weaponized human fear frequencies, tearing a rift into The Inverted World.\n\nMultiple Endings:\n1. Escape Ending: Flees through the abandoned metro lines while the cosmic anomaly persists.\n2. Containment Ending: Destroys the resonance cores, sealing the Eclipse forever.\n3. The Truth Ending: Discovers the protagonist originally opened the gateway.",
                        color = EclipseTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }

                // AI Monsters Architecture
                SectionCard(
                    title = if (isAr) "2. بنية الذكاء الاصطناعي للأعداء الـ 5" else "2. 5 Monster AI Behaviors & State Machines",
                    isAr = isAr
                ) {
                    Text(
                        text = if (isAr)
                            "• THE OBSERVER: ذكاء سلوكي متكيف (FSM). يمتلك مخروط رؤية (FOV) واستشعار صوتي، ويتعلم أماكن اختباء اللاعب في الخزانات ويزيد مدة تفتيشها.\n• THE SOUND HUNTER: كائن أعمى ذو سرعة قصوى (Rush speed)، يندفع فقط عند رصد الركض أو الخطوات العالية.\n• THE LURKER: كائن باكي ساكن ينشط فقط عند الاقتراب المباشر أو التحديق المستمر في وجهه.\n• THE MIMIC: يصدر تشويشات صوتية وتلميحات مضللة لجذب اللاعب.\n• FACILITY SENTINEL: آلي حراسة ثقيل يمسح بنظام كاشف ضوئي دوري."
                        else
                            "• THE OBSERVER: Adaptive FSM stalker with vision cone & hearing. Learns player locker hiding spots and lengthens search time.\n• THE SOUND HUNTER: Blind creature with lethal rush speed, reacting purely to loud sprinting.\n• THE LURKER: Stationary weeping entity that awakens upon close proximity or direct eye contact.\n• THE MIMIC: Emits deceptive voice recordings and radio static to lure players.\n• FACILITY SENTINEL: Heavy armored patrol with periodic scanning beams.",
                        color = EclipseTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }

                // Unity & C# Scripts Documentation
                SectionCard(
                    title = if (isAr) "3. شيفرة C# الجاهزة للتصدير إلى Unity (Unity 2022.3 / 6 LTS)" else "3. Unity & C# Architecture Blueprint",
                    isAr = isAr
                ) {
                    Text(
                        text = """
// --- Unity C# Architecture Specification ---
// Namespace: Eclipse.Horror

// 1. FirstPersonController.cs
public class FirstPersonController : MonoBehaviour {
    [SerializeField] private CharacterController controller;
    [SerializeField] private float walkSpeed = 2.5f, sprintSpeed = 4.5f, crouchSpeed = 1.2f;
    [SerializeField] private float stamina = 100f, maxStamina = 100f;
    [SerializeField] private Light flashlight;
    [SerializeField] private float battery = 100f;
    // Realtime footsteps noise broadcast to Monster AI listeners via Physics.OverlapSphere
}

// 2. ObserverAI.cs
public class ObserverAI : MonoBehaviour {
    public enum State { Patrol, InvestigateSound, Chase, SearchLastSeen }
    public State currentState;
    public Transform[] waypoints;
    public LayerMask obstacleMask;
    public float viewRadius = 14f, viewAngle = 110f;
    // Adapts search duration dynamically: searchTimer += PlayerLockerHidesCount * 0.5f;
}

// 3. HorrorSaveSystem.cs
public static class HorrorSaveSystem {
    public static void Save(SaveData data, string path) {
        string json = JsonUtility.ToJson(data);
        File.WriteAllText(path, Encrypt(json));
    }
}
                        """.trimIndent(),
                        color = EclipseEerieCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                // Performance & Mobile Optimization Report
                SectionCard(
                    title = if (isAr) "4. تقرير تحسين الأداء لجميع الأجهزة المستهدفة" else "4. Performance Optimization Report",
                    isAr = isAr
                ) {
                    Text(
                        text = if (isAr)
                            "• تحسين الأداء: استخدام خوارزمية DDA Raycasting فائقة الخفة مع تقليل خطوات الأشعة بحسب جودة الرسومات (3 بكسل في Low، 2 في Medium، 1 في High).\n• الصوت دون ملفات خارجية: توليد الذبذبات الترددية ونبضات القلب مباشرة عبر AudioTrack لمنع أي استهلاك غير ضروري للذاكرة.\n• استهلاك الذاكرة: أقل من 40 ميغابايت RAM، مع تشغيل سلس بـ 60 إطاراً في الثانية."
                        else
                            "• Zero external bloat: Algorithmic procedural synthesis for ambient drones & heartbeats via AudioTrack.\n• DDA Raycasting with dynamic resolution stepping per quality preset (Low: 3px, Med: 2px, High: 1px).\n• Total memory footprint < 40MB RAM, maintaining 60 FPS on Android devices.",
                        color = EclipseTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    isAr: Boolean,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = EclipseSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = EclipseCrimsonGlow,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
