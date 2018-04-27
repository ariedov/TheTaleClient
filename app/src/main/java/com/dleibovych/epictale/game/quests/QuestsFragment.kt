package com.dleibovych.epictale.game.quests

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.dleibovych.epictale.R
import com.dleibovych.epictale.api.model.QuestActorPlaceInfo
import com.dleibovych.epictale.api.model.QuestActorSpendingInfo
import com.dleibovych.epictale.game.di.GameComponentProvider
import kotlinx.android.synthetic.main.fragment_quests.*

import java.util.HashMap

import org.thetale.api.enumerations.QuestType
import org.thetale.api.models.GameInfo
import org.thetale.api.models.QuestActorPersonInfo
import javax.inject.Inject

class QuestsFragment : Fragment(), QuestsView {

    @Inject
    lateinit var presenter: QuestsPresenter

    private val actorNames = HashMap<TextView, Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity!!.application as GameComponentProvider)
                .provideGameComponent()
                ?.inject(this)

        presenter.view = this

        return layoutInflater.inflate(R.layout.fragment_quests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        error.onRetryClick(View.OnClickListener {
            presenter.loadQuests()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        presenter.view = null
    }

    override fun onStart() {
        super.onStart()

        presenter.start()
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
        error.visibility = View.GONE
        content.visibility = View.GONE
    }

    override fun showQuests(info: GameInfo) {
        content.visibility = View.VISIBLE
        error.visibility = View.GONE
        progress.visibility = View.GONE

        questsContainer.removeAllViews()
        actorNames.clear()

        val questLine = info.account!!.hero.quests.quests.last()
        val questStep = questLine.line.last()
        val questStepView = layoutInflater!!.inflate(R.layout.item_quest, questsContainer, false)

        val questNameView = questStepView.findViewById<View>(R.id.quest_name) as TextView
        val rewards: String?
        rewards = if (questStep.experience > 0 && questStep.power == 0) {
            String.format(" (%s)", getString(R.string.quest_reward_experience, questStep.experience))
        } else if (questStep.experience == 0 && questStep.power > 0) {
            String.format(" (%s)", getString(R.string.quest_reward_power, questStep.power))
        } else if (questStep.experience > 0 && questStep.power > 0) {
            String.format(" (%s, %s)",
                    getString(R.string.quest_reward_experience, questStep.experience),
                    getString(R.string.quest_reward_power, questStep.power))
        } else {
            null
        }

        if (TextUtils.isEmpty(rewards)) {
            questNameView.text = questStep.name
        } else {
            val rewardsString = SpannableString(rewards)
            rewardsString.setSpan(ForegroundColorSpan(resources.getColor(R.color.game_additional_info)),
                    0, rewardsString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            questNameView.text = TextUtils.concat(questStep.name, rewardsString)
        }

        (questStepView.findViewById<View>(R.id.quest_icon) as ImageView)
                .setImageResource(QuestType.getQuestType(questStep.type).drawableResId)

        val actorsContainer = questStepView.findViewById<View>(R.id.quest_actors_container) as ViewGroup

        for (actorInfo in questStep.actors) {

            val actorView = layoutInflater!!.inflate(R.layout.item_text, actorsContainer, false)

            val actorTextView = actorView.findViewById<View>(R.id.item_text_content) as TextView
            val actorText: CharSequence
            val actorName = SpannableString(actorInfo.name)
            actorName.setSpan(StyleSpan(Typeface.BOLD), 0, actorName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val actor = actorInfo.actor
            when (actor) {
                is QuestActorPersonInfo -> {
                    actorText = TextUtils.concat(actorName, ": ", actor.name)
                    actorNames[actorTextView] = actor.place
                }

                is QuestActorPlaceInfo -> {
                    actorText = TextUtils.concat(actorName, ": ", actor.name)
                }

                is QuestActorSpendingInfo -> {
                    actorText = TextUtils.concat(actorName, ": ", actor.goal)
                }

                else -> actorText = actorName
            }
            actorTextView.text = actorText

            actorsContainer.addView(actorView)

            if (info.account!!.isOwn) {
                val choicesContainer = questStepView.findViewById<View>(R.id.quest_choices_container) as ViewGroup
                val choices = questStep.choiceAlternatives
                if (choices != null) {
                    for (choice in choices) {
                        val choiceView = layoutInflater!!.inflate(R.layout.item_quest_choice, choicesContainer, false)

                        val choiceDescription = SpannableString(choice[1])
                        choiceDescription.setSpan(ForegroundColorSpan(resources.getColor(R.color.common_link)),
                                0, choice[1].length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        val choiceTextView = choiceView.findViewById<View>(R.id.quest_choice) as TextView
                        choiceTextView.text = TextUtils.concat(getString(R.string.quest_choice_part), choiceDescription)
                        choiceTextView.setOnClickListener { presenter.chooseQuestOption(choice[0].toInt()) }

                        choicesContainer.addView(choiceView)
                    }
                }
            }
        }

        questsContainer.addView(questStepView)
    }

    override fun showError() {
        error.visibility = View.VISIBLE
        progress.visibility = View.GONE
        content.visibility = View.GONE

        error.setErrorText(getString(R.string.common_error))
    }

    override fun showQuestActionProgress() {

    }

    override fun showQuestActionError() {

    }

    override fun onDestroy() {
        super.onDestroy()

        if (activity!!.isFinishing) {
            presenter.dispose()
        }
    }

    companion object {

        fun create() = QuestsFragment()
    }
}
