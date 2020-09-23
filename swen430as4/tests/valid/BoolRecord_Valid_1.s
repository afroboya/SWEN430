
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq $1, %rbx
	movq %rbx, 8(%rax)
	movq $0, %rbx
	movq %rbx, 16(%rax)
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label558
	movq $1, %rax
	jmp label559
label558:
	movq $0, %rax
label559:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq 16(%rax), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label560
	movq $1, %rax
	jmp label561
label560:
	movq $0, %rax
label561:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $24, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $2, %rcx
	movq %rcx, 0(%rbx)
	movq $1, %rcx
	movq %rcx, 8(%rbx)
	movq $0, %rcx
	movq %rcx, 16(%rbx)
	jmp label562
	movq $1, %rax
	jmp label563
label562:
	movq $0, %rax
label563:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $0, %rbx
	movq %rbx, 8(%rax)
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label564
	movq $1, %rax
	jmp label565
label564:
	movq $0, %rax
label565:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq 16(%rax), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label566
	movq $1, %rax
	jmp label567
label566:
	movq $0, %rax
label567:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $24, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $2, %rcx
	movq %rcx, 0(%rbx)
	movq $0, %rcx
	movq %rcx, 8(%rbx)
	movq $0, %rcx
	movq %rcx, 16(%rbx)
	jmp label568
	movq $1, %rax
	jmp label569
label568:
	movq $0, %rax
label569:
	movq %rax, %rdi
	call assertion
label557:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
