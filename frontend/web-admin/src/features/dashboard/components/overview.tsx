import { Bar, BarChart, ResponsiveContainer, XAxis, YAxis, Tooltip, Legend } from 'recharts'
import { MonthlyChartItem } from '../data/use-dashboard-stats'

interface OverviewProps {
  data?: MonthlyChartItem[]
}

const defaultMonthlyData: MonthlyChartItem[] = [
  { name: 'Th01', posts: 0, ai: 0, total: 0 },
  { name: 'Th02', posts: 0, ai: 0, total: 0 },
  { name: 'Th03', posts: 0, ai: 0, total: 0 },
  { name: 'Th04', posts: 0, ai: 0, total: 0 },
  { name: 'Th05', posts: 0, ai: 0, total: 0 },
  { name: 'Th06', posts: 0, ai: 0, total: 0 },
  { name: 'Th07', posts: 0, ai: 0, total: 0 },
  { name: 'Th08', posts: 0, ai: 0, total: 0 },
  { name: 'Th09', posts: 0, ai: 0, total: 0 },
  { name: 'Th10', posts: 10, ai: 3, total: 13 },
  { name: 'Th11', posts: 21, ai: 8, total: 29 },
  { name: 'Th12', posts: 0, ai: 0, total: 0 },
]

export function Overview({ data = defaultMonthlyData }: OverviewProps) {
  const chartData = data && data.length > 0 ? data : defaultMonthlyData

  return (
    <ResponsiveContainer width='100%' height={350}>
      <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
        <XAxis
          dataKey='name'
          stroke='#888888'
          fontSize={12}
          tickLine={false}
          axisLine={false}
        />
        <YAxis
          stroke='#888888'
          fontSize={12}
          tickLine={false}
          axisLine={false}
          allowDecimals={false}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: 'hsl(var(--card))',
            borderColor: 'hsl(var(--border))',
            borderRadius: '8px',
            color: 'hsl(var(--card-foreground))',
          }}
          formatter={(value: any, name: any) => [
            `${value} lượt`,
            name === 'posts' ? 'Bài viết ẩm thực' : 'Gợi ý AI',
          ]}
        />
        <Legend
          verticalAlign='top'
          align='right'
          height={36}
          formatter={(value) => (value === 'posts' ? 'Bài viết ẩm thực' : 'Gợi ý AI')}
        />
        <Bar
          dataKey='posts'
          name='posts'
          fill='#f97316'
          radius={[4, 4, 0, 0]}
        />
        <Bar
          dataKey='ai'
          name='ai'
          fill='#8b5cf6'
          radius={[4, 4, 0, 0]}
        />
      </BarChart>
    </ResponsiveContainer>
  )
}
