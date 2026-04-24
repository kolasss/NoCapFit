@file:Suppress("unused", "MagicNumber")

package com.example.nocapfit.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Indigo (accent) ──
// Derived from oklch(62% 0.14 262) dark / oklch(52% 0.14 262) light
val Indigo20 = Color(0xFF1B2B55)
val Indigo30 = Color(0xFF243E7C)
val Indigo40 = Color(0xFF3A65B8) // light accent
val Indigo50 = Color(0xFF4A75CB)
val Indigo60 = Color(0xFF5684DA) // dark accent
val Indigo70 = Color(0xFF7298E0)
val Indigo80 = Color(0xFF8DB7FF) // dark accent text / light on dark
val Indigo90 = Color(0xFFD9E2FC) // light accent container
val Indigo95 = Color(0xFFEDF1FD)
val IndigoOnContainerDark = Color(0xFF8DB7FF)
val IndigoOnContainerLight = Color(0xFF2750A2)

// Dim accent-tinted row/background — computed at 20% over bg / 14% over light bg
val IndigoTintDark = Color(0xFF1C2638) // indigo @ 20% on #0E0E10
val IndigoTintLight = Color(0xFFD8DEEC) // indigo @ 14% on #F2F2F5

// ─── Amber (timer accent) ──
// oklch(72% 0.16 60) dark / oklch(58% 0.16 60) light
val Amber30 = Color(0xFF5B3708)
val Amber40 = Color(0xFF9A4E00)
val Amber50 = Color(0xFFBC5D00) // light amber
val Amber60 = Color(0xFFEB881F) // dark amber
val Amber80 = Color(0xFFFFC590)
val Amber90 = Color(0xFFFFE0C2)

val AmberTintDark = Color(0xFF2D1F12) // amber @ 14% on #0E0E10
val AmberTintLight = Color(0xFFEBE0D8) // amber @ 12% on #F2F2F5

// ─── Error / danger ──
val Danger40 = Color(0xFFBB061E)
val Danger60 = Color(0xFFDE3B3D)
val Danger90 = Color(0xFFFFDAD6)
val Danger20 = Color(0xFF4A0009)

// ─── Neutrals — tuned to design (#0e0e10 / #1c1c1f / #28282d / #323237 / #f2f2f5 / #fff) ──
val Gray0 = Color(0xFF000000)
val Gray05 = Color(0xFF0E0E10) // dark bg
val Gray10 = Color(0xFF161618)
val Gray15 = Color(0xFF1C1C1F) // dark surface
val Gray20 = Color(0xFF28282D) // dark surface2
val Gray25 = Color(0xFF323237) // dark surface3
val Gray30 = Color(0xFF3D3D43)
val Gray50 = Color(0xFF55555F)
val Gray60 = Color(0xFF6A6A78)
val Gray70 = Color(0xFF8A8A96)
val Gray80 = Color(0xFFAAAABC)
val Gray90 = Color(0xFFD9D9DE)
val Gray94 = Color(0xFFEBEBEF)
val Gray96 = Color(0xFFF2F2F5) // light bg
val Gray98 = Color(0xFFF7F7FA) // light surface2
val Gray100 = Color(0xFFFFFFFF) // light surface

val OnDarkText = Color(0xFFF0F0F2)
val OnLightText = Color(0xFF111114)
