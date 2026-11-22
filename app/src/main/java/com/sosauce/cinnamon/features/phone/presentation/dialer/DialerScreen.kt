@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.features.phone.presentation.dialer

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.paging.LoadState
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.paging.compose.LazyPagingItems
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.app.navigation.Screen
import com.sosauce.cinnamon.core.ui.components.ScreenSelection
import com.sosauce.cinnamon.core.ui.components.buttons.LongClickButton
import com.sosauce.cinnamon.core.ui.components.items.CuteListItem
import com.sosauce.cinnamon.core.utils.backspace
import com.sosauce.cinnamon.core.utils.PermissionUtils
import com.sosauce.cinnamon.core.utils.LazyListKeys
import com.sosauce.cinnamon.core.utils.rememberFocusRequester
import com.sosauce.cinnamon.features.contacts.domain.CuteContact
import com.sosauce.cinnamon.features.contacts.presentation.ContactListItem
import com.sosauce.cinnamon.features.contacts.presentation.ContactsListItem
import com.sosauce.cinnamon.features.contacts.presentation.components.FavoriteContact
import com.sosauce.cinnamon.features.contacts.presentation.components.dialogs.NumberPickerDialog
import com.sosauce.cinnamon.features.contacts.presentation.contactsList
import com.sosauce.cinnamon.features.phone.domain.CallPresentation
import com.sosauce.cinnamon.features.phone.domain.SimChoice
import com.sosauce.cinnamon.features.phone.presentation.call.CallAction
import com.sosauce.cinnamon.features.phone.presentation.logs.CallLogItem
import com.sosauce.cinnamon.features.phone.presentation.logs.CallLogsAction
import com.sosauce.cinnamon.features.phone.presentation.logs.CallLogsListItem
import com.sosauce.cinnamon.features.phone.presentation.logs.CallLogsState
import com.sosauce.cinnamon.features.phone.presentation.call.components.DisableSoftKeyboard
import com.sosauce.nekobites.animations.AnimatedFab
import com.sosauce.nekobites.components.LoadingBox
import com.sosauce.nekobites.components.NoXFound

@Composable
fun SharedTransitionScope.DialerScreen(
    state: DialerState,
    contacts: LazyPagingItems<ContactsListItem>,
    recentCalls: LazyPagingItems<CallLogsListItem>,
    callLogsState: CallLogsState,
    simChoices: List<SimChoice>,
    textFieldState: TextFieldState,
    onNavigate: (Screen) -> Unit,
    onNavigateUp: () -> Unit,
    onHandleCallAction: (CallAction) -> Unit,
    onHandleCallLogsActions: (CallLogsAction) -> Unit
) {
    val isDefaultDialer by PermissionUtils.observeHasDefaultApp(PermissionUtils.DIALER)
    LaunchedEffect(isDefaultDialer) {
        onHandleCallLogsActions(CallLogsAction.DefaultDialerChanged(isDefaultDialer))
    }
    val dialpadLayout = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#")
    )
    val t9Map = mapOf(
        "2" to "ABC",
        "3" to "DEF",
        "4" to "GHI",
        "5" to "JKL",
        "6" to "MNO",
        "7" to "PQRS",
        "8" to "TUV",
        "9" to "WXYZ",
        "0" to "+"
    )
    val focusRequester = rememberFocusRequester()
    var numberPickerContact by remember { mutableStateOf<CuteContact?>(null) }

    numberPickerContact?.let { contact ->
        NumberPickerDialog(
            onDismissRequest = { numberPickerContact = null },
            onPickNumber = { number ->
                numberPickerContact = null
                onHandleCallAction(CallAction.LaunchCall(number))
            },
            phoneNumbers = contact.phoneNumbers.fastMap { it.number }
        )
    }


    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DisableSoftKeyboard {
                        TextField(
                            state = textFieldState,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.enter_number)
                                )
                            },
                            leadingIcon = {
                                IconButton(
                                    onClick = onNavigateUp,
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.back),
                                        contentDescription = null
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { textFieldState.backspace() },
                                    shapes = IconButtonDefaults.shapes(),
                                    enabled = textFieldState.text.isNotEmpty()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.backspace),
                                        contentDescription = stringResource(R.string.backspace)
                                    )
                                }
                            }
                        )
                    }
                }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        dialpadLayout.fastForEach { rowKeys ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                rowKeys.fastForEach { number ->
                                    val letters = t9Map[number] ?: ""
                                    val onLongClick = remember {
                                        if (letters == "+") {
                                            { textFieldState.edit { insert(selection.start, "+") } }
                                        } else null
                                    }

                                    LongClickButton(
                                        onClick = {
                                            textFieldState.edit {
                                                insert(textFieldState.text.length, number)
                                            }
                                        },
                                        onLongClick = onLongClick,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(64.dp),
                                        contentPadding = ButtonDefaults.ContentPadding,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        )
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = number,
                                                style = MaterialTheme.typography.headlineSmallEmphasized.copy(
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            )
                                            Text(
                                                text = letters,
                                                style = MaterialTheme.typography.labelSmallEmphasized.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        val number = textFieldState.text.toString()
                        val sim1 = simChoices.getOrNull(0)
                        val sim2 = simChoices.getOrNull(1)
                        Row(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledIconButton(
                                onClick = {
                                    onHandleCallAction(CallAction.LaunchCall(number, sim1?.handle, true))
                                },
                                shapes = IconButtonDefaults.shapes(),
                                modifier = Modifier.weight(15f).height(56.dp),
                                enabled = number.isNotBlank() && sim1?.supportsVideoCall == true
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.camera),
                                    contentDescription = "SIM 1 video call"
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    onHandleCallAction(CallAction.LaunchCall(number, sim1?.handle))
                                },
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier.weight(35f).height(56.dp),
                                enabled = number.isNotBlank() && (sim1 != null || simChoices.isEmpty())
                            ) {
                                Icon(painter = painterResource(R.drawable.call), contentDescription = null)
                                Text("SIM 1")
                            }
                            FilledTonalButton(
                                onClick = {
                                    onHandleCallAction(CallAction.LaunchCall(number, sim2?.handle))
                                },
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier.weight(35f).height(56.dp),
                                enabled = number.isNotBlank() && sim2 != null
                            ) {
                                Icon(painter = painterResource(R.drawable.call), contentDescription = null)
                                Text("SIM 2")
                            }
                            FilledIconButton(
                                onClick = {
                                    onHandleCallAction(CallAction.LaunchCall(number, sim2?.handle, true))
                                },
                                shapes = IconButtonDefaults.shapes(),
                                modifier = Modifier.weight(15f).height(56.dp),
                                enabled = number.isNotBlank() && sim2?.supportsVideoCall == true
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.camera),
                                    contentDescription = "SIM 2 video call"
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LoadingBox(
                    isLoading = if (textFieldState.text.isBlank()) callLogsState.isLoading else state.isLoading
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)
                    ) {
                if (textFieldState.text.isBlank()) {
                    if (
                        recentCalls.itemCount == 0 &&
                        recentCalls.loadState.refresh is LoadState.NotLoading &&
                        !callLogsState.isLoading
                    ) {
                        item(LazyListKeys.EMPTY_STATE) {
                            NoXFound(
                                headlineText = R.string.no_calls_found,
                                bodyText = R.string.no_calls_found_desc,
                                icon = R.drawable.call_log
                            )
                        }
                    } else {
                        items(
                            count = recentCalls.itemCount,
                            key = recentCalls.itemKey { it.key },
                            contentType = recentCalls.itemContentType { it::class }
                        ) { index ->
                            when (val item = recentCalls[index]) {
                                is CallLogsListItem.Header -> {
                                    Text(
                                        text = item.date,
                                        style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                    )
                                }
                                is CallLogsListItem.Log -> {
                                    val callLog = item.log
                                    val previousSimHandle = callLog.sim?.subId?.let { subId ->
                                        simChoices.firstOrNull { it.subId == subId }?.handle
                                    }
                                    CallLogItem(
                                        callLog = callLog,
                                        simHandleForCall = previousSimHandle,
                                        isSelected = false,
                                        numberOfAppearance = item.count,
                                        onCallAction = onHandleCallAction,
                                        onNavigate = onNavigate,
                                        onDeleteCallLog = {
                                            onHandleCallLogsActions(CallLogsAction.DeleteLogs(listOf(callLog.id)))
                                        },
                                        onClick = {
                                            if (callLog.presentation == CallPresentation.ALLOWED) {
                                                onHandleCallAction(
                                                    CallAction.LaunchCall(callLog.number, previousSimHandle)
                                                )
                                            }
                                        },
                                        onLongClick = {}
                                    )
                                }
                                null -> Unit
                            }
                        }
                    }
                } else {
                val onContactClick: (CuteContact) -> Unit = { contact ->
                    if (contact.phoneNumbers.size > 1) {
                        numberPickerContact = contact
                    } else {
                        onHandleCallAction(CallAction.LaunchCall(contact.phoneNumbers.first().number))
                    }
                }

                contactsList(
                    favorites = emptyList(),
                    contacts = contacts,
                    emptyState = {
                        if (textFieldState.text.isEmpty()) {
                            NoXFound(
                                headlineText = R.string.no_contacts_found,
                                bodyText = R.string.no_contacts_found_desc,
                                icon = R.drawable.contacts
                            )
                        } else {
                            val number = textFieldState.text.toString()
                            CuteListItem(
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                                onClick = {
                                    onNavigate(
                                        Screen.ContactEditor(
                                            rawContactId = null,
                                            prefilledNumber = number
                                        )
                                    )
                                },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .size(50.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialShapes.Cookie9Sided.toShape()
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.add),
                                            contentDescription = null,
                                            tint = contentColorFor(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.add_contact),
                                    style = MaterialTheme.typography.bodyLargeEmphasized
                                )
                                Text(
                                    text = number,
                                    style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    },
                    favoriteContactItem = { contact ->


                        FavoriteContact(
                            contact = contact,
                            isSelected = false,
                            onClick = { onContactClick(contact) },
                            onLongClick = {},
                            showNumber = true
                        )
                    }
                ) { contact ->
                    ContactListItem(
                        contact = contact,
                        isSelected = false,
                        onClick = { onContactClick(contact) },
                        showNumber = true
                    )
                }
                }
            }
        }

            }
        }
    }

}